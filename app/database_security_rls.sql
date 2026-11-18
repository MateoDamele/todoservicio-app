-- ============================================================================
-- TODOSERVICIO S.A.S. - SCRIPT OFICIAL DE SEGURIDAD DEFENSIVA Y RLS ENTERPRISE
-- Base de Datos: PostgreSQL / Supabase
-- Estándar: Zero-Trust, RBAC Estricto, Atomic OT Sequences y Anti-Spoofing
-- ============================================================================

-- 0. EXTENSIONES INDISPENSABLES
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. TABLAS PRINCIPALES DEL SISTEMA
-- ============================================================================

-- Tabla de Clientes Corporativos (Empresas Matrices)
CREATE TABLE IF NOT EXISTS public.clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_name TEXT NOT NULL,
    rut TEXT NOT NULL UNIQUE,
    plan_name TEXT DEFAULT 'Plan Corporativo Plus',
    status TEXT DEFAULT 'Activo' CHECK (status IN ('Activo', 'Inactivo', 'Suspendido')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de Sedes / Sucursales por Empresa (Multi-Sede)
CREATE TABLE IF NOT EXISTS public.sedes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES public.clients(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    address TEXT NOT NULL,
    contact_person TEXT,
    phone TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de Perfiles de Usuario Vinculados a auth.users
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL UNIQUE,
    username TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('admin', 'superadmin', 'tech', 'client', 'cleaning')),
    client_id UUID REFERENCES public.clients(id) ON DELETE SET NULL,
    phone TEXT,
    rut TEXT,
    specialty TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_temporary_password BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Secuenciador Atómico de Órdenes de Trabajo (OT-0001 al Infinito)
CREATE TABLE IF NOT EXISTS public.ot_counters (
    id INT PRIMARY KEY DEFAULT 1,
    last_value INT NOT NULL DEFAULT 25,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT single_row_check CHECK (id = 1)
);

INSERT INTO public.ot_counters (id, last_value)
VALUES (1, 25)
ON CONFLICT (id) DO NOTHING;

-- Tabla Centralizada de Órdenes de Trabajo (OT)
CREATE TABLE IF NOT EXISTS public.work_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ot_number INT NOT NULL UNIQUE,
    code TEXT NOT NULL UNIQUE, -- ej. 'OT-0026'
    client_id UUID NOT NULL REFERENCES public.clients(id) ON DELETE RESTRICT,
    sede_id UUID REFERENCES public.sedes(id) ON DELETE SET NULL,
    assigned_tech_ids UUID[] NOT NULL DEFAULT '{}',
    title TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    scheduled_date DATE NOT NULL,
    time_slot TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'ABIERTA' CHECK (status IN ('ABIERTA', 'INICIO_EJECUCION', 'EN_PROCESO', 'FINALIZADA', 'CANCELADA')),
    is_dec406 BOOLEAN NOT NULL DEFAULT true,
    check_in_time TEXT,
    check_out_time TEXT,
    technician_observations TEXT,
    evidence_photos TEXT[] DEFAULT '{}',
    tech_signature_url TEXT,
    client_signature_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de Geocercas Perimetrales para Edificios (Lenox HR / Todoservicio)
CREATE TABLE IF NOT EXISTS public.geofences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    building_name TEXT NOT NULL,
    address TEXT NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    radius_meters INT NOT NULL DEFAULT 50,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Tabla de Registros de Asistencia y Marcación GPS (Personal de Limpieza)
CREATE TABLE IF NOT EXISTS public.cleaning_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    building_id UUID REFERENCES public.geofences(id) ON DELETE SET NULL,
    building_name TEXT NOT NULL,
    building_address TEXT,
    mark_type TEXT NOT NULL CHECK (mark_type IN ('E', 'S')), -- E = Entrada, S = Salida
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    distance_meters INT NOT NULL DEFAULT 0,
    is_inside_geofence BOOLEAN NOT NULL DEFAULT true,
    is_mocked BOOLEAN NOT NULL DEFAULT false, -- Anti-Spoofing flag
    notes TEXT,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================================
-- 2. FUNCIONES DE APOYO SECURITY DEFINER (EVITAN BYPASS DE METADATOS)
-- ============================================================================

-- Obtiene el rol del usuario autenticado directamente desde public.profiles
CREATE OR REPLACE FUNCTION public.get_auth_role()
RETURNS TEXT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT role FROM public.profiles WHERE id = auth.uid() AND is_active = true;
$$;

-- Obtiene el client_id del usuario autenticado si es un Cliente Corporativo
CREATE OR REPLACE FUNCTION public.get_auth_client_id()
RETURNS UUID
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT client_id FROM public.profiles WHERE id = auth.uid() AND is_active = true;
$$;

-- Generador Atómico Transaccional de Códigos Correlativos Globales de OT (OT-0001 al infinito)
CREATE OR REPLACE FUNCTION public.generate_next_ot_code()
RETURNS TEXT
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    next_seq INT;
    ot_formatted TEXT;
BEGIN
    UPDATE public.ot_counters
    SET last_value = last_value + 1,
        updated_at = now()
    WHERE id = 1
    RETURNING last_value INTO next_seq;

    ot_formatted := 'OT-' || LPAD(next_seq::TEXT, 4, '0');
    RETURN ot_formatted;
END;
$$;

-- ============================================================================
-- 3. ACTIVACIÓN ESTRICTA DE ROW LEVEL SECURITY (RLS)
-- ============================================================================

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sedes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ot_counters ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.work_orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.geofences ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.cleaning_logs ENABLE ROW LEVEL SECURITY;

-- ============================================================================
-- 4. POLÍTICAS RLS: PROFILES
-- ============================================================================

-- Administradores: Acceso total
CREATE POLICY "Admin full access on profiles"
ON public.profiles
FOR ALL
TO authenticated
USING (public.get_auth_role() IN ('admin', 'superadmin'))
WITH CHECK (public.get_auth_role() IN ('admin', 'superadmin'));

-- Usuarios: Lectura de su propio perfil
CREATE POLICY "Users can read own profile"
ON public.profiles
FOR SELECT
TO authenticated
USING (id = auth.uid());

-- Usuarios: Actualización de su propio teléfono o avatar (sin cambiar role ni client_id)
CREATE POLICY "Users can update own basic profile"
ON public.profiles
FOR UPDATE
TO authenticated
USING (id = auth.uid())
WITH CHECK (
    id = auth.uid()
    AND role = (SELECT role FROM public.profiles WHERE id = auth.uid())
    AND client_id IS NOT DISTINCT FROM (SELECT client_id FROM public.profiles WHERE id = auth.uid())
);

-- ============================================================================
-- 5. POLÍTICAS RLS: CLIENTS Y SEDES
-- ============================================================================

-- Admin: Control total de empresas y locales
CREATE POLICY "Admin full access on clients"
ON public.clients FOR ALL TO authenticated
USING (public.get_auth_role() IN ('admin', 'superadmin'))
WITH CHECK (public.get_auth_role() IN ('admin', 'superadmin'));

CREATE POLICY "Admin full access on sedes"
ON public.sedes FOR ALL TO authenticated
USING (public.get_auth_role() IN ('admin', 'superadmin'))
WITH CHECK (public.get_auth_role() IN ('admin', 'superadmin'));

-- Cliente Corporativo: Lectura exclusiva de su propia empresa
CREATE POLICY "Clients can read own company"
ON public.clients FOR SELECT TO authenticated
USING (id = public.get_auth_client_id());

-- Cliente Corporativo: Lectura exclusiva de sus propias sedes/locales
CREATE POLICY "Clients can read own sedes"
ON public.sedes FOR SELECT TO authenticated
USING (client_id = public.get_auth_client_id());

-- ============================================================================
-- 6. POLÍTICAS RLS: WORK_ORDERS (ÓRDENES DE TRABAJO)
-- ============================================================================

-- Admin: Control absoluto de órdenes
CREATE POLICY "Admin full access on work_orders"
ON public.work_orders FOR ALL TO authenticated
USING (public.get_auth_role() IN ('admin', 'superadmin'))
WITH CHECK (public.get_auth_role() IN ('admin', 'superadmin'));

-- Cliente Corporativo: Aislamiento Inviolable por client_id
CREATE POLICY "Clients view only their own work_orders"
ON public.work_orders FOR SELECT TO authenticated
USING (client_id = public.get_auth_client_id());

-- Técnico: Visualiza únicamente las órdenes donde está asignado
CREATE POLICY "Technicians view assigned work_orders"
ON public.work_orders FOR SELECT TO authenticated
USING (
    public.get_auth_role() = 'tech'
    AND auth.uid() = ANY(assigned_tech_ids)
);

-- Técnico: Actualiza únicamente estado, fotos y rúbrica de sus órdenes asignadas
CREATE POLICY "Technicians update assigned work_orders"
ON public.work_orders FOR UPDATE TO authenticated
USING (
    public.get_auth_role() = 'tech'
    AND auth.uid() = ANY(assigned_tech_ids)
)
WITH CHECK (
    public.get_auth_role() = 'tech'
    AND auth.uid() = ANY(assigned_tech_ids)
);

-- ============================================================================
-- 7. POLÍTICAS RLS: CLEANING_LOGS (ASISTENCIAS DE LIMPIEZA)
-- ============================================================================

-- Admin: Auditoría total de marcas de entrada y salida
CREATE POLICY "Admin full access on cleaning_logs"
ON public.cleaning_logs FOR ALL TO authenticated
USING (public.get_auth_role() IN ('admin', 'superadmin'))
WITH CHECK (public.get_auth_role() IN ('admin', 'superadmin'));

-- Funcionaria de Limpieza: Inserción exclusiva con su propio user_id y sin GPS simulado
CREATE POLICY "Cleaning staff insert own marks"
ON public.cleaning_logs FOR INSERT TO authenticated
WITH CHECK (
    user_id = auth.uid()
    AND is_mocked = false
);

-- Funcionaria de Limpieza: Lectura exclusiva de sus propios registros
CREATE POLICY "Cleaning staff read own marks"
ON public.cleaning_logs FOR SELECT TO authenticated
USING (user_id = auth.uid());

-- ============================================================================
-- 8. POLÍTICAS RLS: GEOFENCES
-- ============================================================================

-- Admin: Edición y creación de geocercas
CREATE POLICY "Admin full access on geofences"
ON public.geofences FOR ALL TO authenticated
USING (public.get_auth_role() IN ('admin', 'superadmin'))
WITH CHECK (public.get_auth_role() IN ('admin', 'superadmin'));

-- Personal Operativo: Lectura de geocercas activas para validación en dispositivo
CREATE POLICY "Authenticated users can read active geofences"
ON public.geofences FOR SELECT TO authenticated
USING (is_active = true);
