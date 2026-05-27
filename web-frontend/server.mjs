import http from "node:http";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const port = Number(process.env.PORT || 5173);
const backend = process.env.BACKEND_URL || "http://localhost:8080";

const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".svg": "image/svg+xml"
};

const server = http.createServer((req, res) => {
  if (!req.url) {
    res.writeHead(400);
    res.end("Bad request");
    return;
  }

  if (req.url.startsWith("/api/")) {
    proxyApi(req, res);
    return;
  }

  const urlPath = decodeURIComponent(new URL(req.url, `http://localhost:${port}`).pathname);
  const safePath = path.normalize(urlPath).replace(/^(\.\.[/\\])+/, "");
  const relativePath = urlPath === "/" ? "index.html" : safePath.replace(/^[/\\]/, "");
  const filePath = path.join(__dirname, relativePath);

  if (!filePath.startsWith(__dirname)) {
    res.writeHead(403);
    res.end("Forbidden");
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
      res.end("Not found");
      return;
    }

    const ext = path.extname(filePath);
    res.writeHead(200, { "Content-Type": mimeTypes[ext] || "application/octet-stream" });
    res.end(data);
  });
});

function proxyApi(clientReq, clientRes) {
  const target = new URL(clientReq.url, backend);
  const proxyReq = http.request(target, {
    method: clientReq.method,
    headers: {
      ...clientReq.headers,
      host: target.host
    }
  }, (proxyRes) => {
    clientRes.writeHead(proxyRes.statusCode || 502, proxyRes.headers);
    proxyRes.pipe(clientRes);
  });

  proxyReq.on("error", () => {
    clientRes.writeHead(502, { "Content-Type": "application/json; charset=utf-8" });
    clientRes.end(JSON.stringify({
      code: 502,
      message: `无法连接后端服务：${backend}`,
      data: null
    }));
  });

  clientReq.pipe(proxyReq);
}

server.listen(port, () => {
  console.log(`QA frontend: http://localhost:${port}`);
  console.log(`Proxy backend: ${backend}`);
});
