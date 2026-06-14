import http from "k6/http";
import { Rate } from "k6/metrics";

const httpStatus = new Rate("http_status");
const statuses = [200, 201, 400, 401, 403, 404, 409, 500, 503];

const example = [
  { endpoints: { getThing: 1, otherThing: 2 }, duration: 10 },
  { endpoints: { getThing: 2, otherThing: 7 }, duration: 20 },
];

function compileWorkload(workload) {
  const scenarios = {};
  const thresholds = {};

  let elapsedSeconds = 0;
  for (let i = 0; i < workload.length; i++) {
    const phase = workload[i];
    for (const [endpoint, rps] of Object.entries(phase.endpoints)) {
      const scenarioName = `${endpoint}_phase_${i + 1}`;
      const phaseTag = `${endpoint}_${i}`;

      scenarios[scenarioName] = {
        executor: "constant-arrival-rate",
        exec: endpoint,

        rate: rps,
        timeUnit: "1s",

        duration: `${phase.duration}s`,
        startTime: `${elapsedSeconds}s`,

        preAllocatedVUs: 10,
        maxVUs: 300,

        env: {
          PHASE: phaseTag,
        },
      };

      const reqDuration = `http_req_duration{phase:${phaseTag}}`;
      thresholds[reqDuration] = [];

      statuses.forEach(code => {
        const statusCodeThreshold = `http_status{phase:${phaseTag},status:${code}}`;
        thresholds[statusCodeThreshold] = [];
      })
    }
    elapsedSeconds += phase.duration;
  }

  return { scenarios, thresholds };
}

export const options = {
  ...compileWorkload(example),
  summaryTrendStats: ["avg", "min", "med", "max", "p(90)", "p(95)", "p(99)"],
};

function recordStatus(status, phase) {
  for (const code of statuses) {
    httpStatus.add(code === status ? 1 : 0, { phase, status: code });
  }
}

export function getThing() {
  const phase = __ENV.PHASE;

  const res = http.get("https://quickpizza.grafana.com", {
    tags: {
      phase,
    },
  });

  recordStatus(res.status, phase);
}

export function otherThing() {
  const phase = __ENV.PHASE;

  const res = http.get("https://quickpizza.grafana.com", {
    tags: {
      phase,
    },
  });

  recordStatus(res.status, phase);
}
