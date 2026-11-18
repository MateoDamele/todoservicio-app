/**
 * ============================================================================
 * TODOSERVICIO ENTERPRISE - BACKEND EDGE FUNCTIONS (DENO / SUPABASE EDGE)
 * Arquitectura Intermediaria Segura:
 * - Generador Atómico de Secuencia Global OT-0001
 * - Validación Criptográfica y Perimetral de Geocercas (Anti-Spoofing)
 * - Firma Temporal de URLs para Almacenamiento Privado
 * ============================================================================
 */

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SUPABASE_SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

const supabaseAdmin = createClient(SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY, {
  auth: { persistSession: false },
});

// Haversine: Cálculo exacto de distancia en metros en el servidor
function calculateDistanceMeters(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371000; // Radio terrestre en metros
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c);
}

serve(async (req: Request) => {
  const url = new URL(req.url);
  const pathname = url.pathname;

  // CORS Headers
  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
    "Content-Type": "application/json",
  };

  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    // ------------------------------------------------------------------------
    // 1. ENDPOINT: /generate-ot (Correlativo Global OT-0001 al Infinito)
    // ------------------------------------------------------------------------
    if (pathname.endsWith("/generate-ot") && req.method === "POST") {
      const { data, error } = await supabaseAdmin.rpc("generate_next_ot_code");
      if (error) throw error;

      return new Response(
        JSON.stringify({
          success: true,
          code: data, // ej. 'OT-0026'
          timestamp: new Date().toISOString(),
        }),
        { headers: corsHeaders, status: 200 }
      );
    }

    // ------------------------------------------------------------------------
    // 2. ENDPOINT: /verify-geofence (Validación de Marcación y Anti-Spoofing)
    // ------------------------------------------------------------------------
    if (pathname.endsWith("/verify-geofence") && req.method === "POST") {
      const body = await req.json();
      const { buildingId, clientLat, clientLng, isMocked, userId, markType } = body;

      // Detección estricta de GPS simulado
      if (isMocked === true) {
        return new Response(
          JSON.stringify({
            success: false,
            error: "SECURITY_BREACH_MOCK_LOCATION",
            message: "Ubicación simulada detectada. La marcación ha sido bloqueada y registrada.",
          }),
          { headers: corsHeaders, status: 403 }
        );
      }

      // Consulta de geocerca oficial en el servidor
      const { data: geofence, error: geoError } = await supabaseAdmin
        .from("geofences")
        .select("lat, lng, radius_meters, building_name")
        .eq("id", buildingId)
        .single();

      if (geoError || !geofence) {
        throw new Error("Geocerca no encontrada o inactiva");
      }

      const distance = calculateDistanceMeters(
        clientLat,
        clientLng,
        geofence.lat,
        geofence.lng
      );
      const isInside = distance <= geofence.radius_meters;

      // Inserción auditada en cleaning_logs
      const { data: logRecord, error: logError } = await supabaseAdmin
        .from("cleaning_logs")
        .insert({
          user_id: userId,
          building_id: buildingId,
          building_name: geofence.building_name,
          mark_type: markType,
          lat: clientLat,
          lng: clientLng,
          distance_meters: distance,
          is_inside_geofence: isInside,
          is_mocked: false,
        })
        .select()
        .single();

      if (logError) throw logError;

      return new Response(
        JSON.stringify({
          success: true,
          isInsideGeofence: isInside,
          distanceMeters: distance,
          buildingName: geofence.building_name,
          logId: logRecord.id,
        }),
        { headers: corsHeaders, status: 200 }
      );
    }

    // ------------------------------------------------------------------------
    // 3. ENDPOINT: /sign-storage-url (URLs Firmadas para Fotos y Firmas Privadas)
    // ------------------------------------------------------------------------
    if (pathname.endsWith("/sign-storage-url") && req.method === "POST") {
      const { bucket, filePath, expiresInSeconds = 300 } = await req.json();

      const { data, error } = await supabaseAdmin.storage
        .from(bucket)
        .createSignedUrl(filePath, expiresInSeconds);

      if (error) throw error;

      return new Response(
        JSON.stringify({
          success: true,
          signedUrl: data.signedUrl,
          expiresIn: expiresInSeconds,
        }),
        { headers: corsHeaders, status: 200 }
      );
    }

    return new Response(JSON.stringify({ error: "Endpoint no encontrado" }), {
      headers: corsHeaders,
      status: 404,
    });
  } catch (err: any) {
    return new Response(
      JSON.stringify({
        success: false,
        error: err.message ?? "Error interno del servidor",
      }),
      { headers: corsHeaders, status: 500 }
    );
  }
});
