const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const PUBLIC_DIR = path.join(__dirname, 'public');

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.webmanifest': 'application/manifest+json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.apk': 'application/vnd.android.package-archive',
  '.pdf': 'application/pdf',
  '.txt': 'text/plain; charset=utf-8'
};

const server = http.createServer((req, res) => {
  // Manejo de CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Range');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const urlPath = req.url.split('?')[0];
  let safePath = path.normalize(decodeURIComponent(urlPath)).replace(/^(\.\.[\/\\])+/, '');
  
  if (safePath === '/' || safePath === '') {
    safePath = '/index.html';
  }

  let filePath = path.join(PUBLIC_DIR, safePath);

  // Intercepción prioritaria para descarga directa del APK con última versión
  if (safePath === '/Todoservicio.apk' || safePath.endsWith('/Todoservicio.apk')) {
    const buildApk = path.join(__dirname, 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk');
    const rootApk = path.join(__dirname, 'Todoservicio.apk');
    const publicApk = path.join(PUBLIC_DIR, 'Todoservicio.apk');

    let apkToServe = null;
    if (fs.existsSync(buildApk)) {
      apkToServe = buildApk;
    } else if (fs.existsSync(rootApk)) {
      apkToServe = rootApk;
    } else if (fs.existsSync(publicApk)) {
      apkToServe = publicApk;
    }

    if (apkToServe) {
      res.setHeader('Content-Disposition', 'attachment; filename="Todoservicio.apk"');
      res.setHeader('Content-Type', 'application/vnd.android.package-archive');
      res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
      res.setHeader('Pragma', 'no-cache');
      res.setHeader('Expires', '0');
      const stat = fs.statSync(apkToServe);
      res.setHeader('Content-Length', stat.size);
      return fs.createReadStream(apkToServe).pipe(res);
    }
  }

  // Comprobar si existe el archivo solicitado
  fs.stat(filePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // SPA Fallback a index.html si no tiene extensión
      if (!path.extname(safePath)) {
        filePath = path.join(PUBLIC_DIR, 'index.html');
        return serveFile(filePath, req, res);
      }

      res.writeHead(404, { 'Content-Type': 'text/plain' });
      res.end('404 Not Found');
      return;
    }

    serveFile(filePath, req, res);
  });
});

function serveFile(filePath, req, res) {
  const ext = path.extname(filePath).toLowerCase();
  let contentType = MIME_TYPES[ext] || 'application/octet-stream';

  if (path.basename(filePath) === 'manifest.json') {
    contentType = 'application/manifest+json; charset=utf-8';
  }

  const stat = fs.statSync(filePath);
  const total = stat.size;

  // Manejo de Descargas y Range (para archivos grandes como APK)
  if (ext === '.apk') {
    res.setHeader('Content-Disposition', 'attachment; filename="Todoservicio.apk"');
  }

  const range = req.headers.range;
  if (range) {
    const parts = range.replace(/bytes=/, "").split("-");
    const partialstart = parts[0];
    const partialend = parts[1];

    const start = parseInt(partialstart, 10);
    const end = partialend ? parseInt(partialend, 10) : total - 1;
    const chunksize = (end - start) + 1;

    res.writeHead(206, {
      'Content-Range': 'bytes ' + start + '-' + end + '/' + total,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': contentType
    });

    const fileStream = fs.createReadStream(filePath, { start: start, end: end });
    fileStream.pipe(res);
  } else {
    res.writeHead(200, {
      'Content-Length': total,
      'Content-Type': contentType,
      'Cache-Control': ext === '.html' ? 'no-cache' : 'public, max-age=3600'
    });
    fs.createReadStream(filePath).pipe(res);
  }
}

server.listen(PORT, '0.0.0.0', () => {
  console.log(`Todoservicio PWA Web Server listening at http://0.0.0.0:${PORT}`);
});
