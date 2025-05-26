const http = require("http");
const process = require("process");
const httpProxy = require("http-proxy");

process.on("SIGINT", () => {
  console.info("Interrupted");
  process.exit(0);
});

const proxy = httpProxy.createProxyServer({});
http
  .createServer((req, res) => {
    console.log("client req");
    setTimeout(() => {
      proxy.web(req, res, { target: "http://localhost:8000" });
    }, 10);
  })
  .listen(8001);
