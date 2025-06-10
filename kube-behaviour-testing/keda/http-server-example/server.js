const http = require("http");
const { randomBytes } = require("crypto");

process.on("SIGINT", () => {
  console.info("Interrupted");
  process.exit(0);
});

let bigOldMemoryLeak = [];

http
  .createServer((req, res) => {
    try {
      const bigThingToTakeUpSomeSpace = randomBytes(1024 * 1024 * 10).toString();
      if (bigOldMemoryLeak.length <= 5) {
        bigOldMemoryLeak.push(bigThingToTakeUpSomeSpace);
      }
      if (req.url.includes("clear-me")) {
        bigOldMemoryLeak = [];
      }
      
      res.end(process.memoryUsage().heapUsed.toString());
    } catch (err) {
      console.error(err);
      res.end("oops");
    }
  })
  .listen(8000);
