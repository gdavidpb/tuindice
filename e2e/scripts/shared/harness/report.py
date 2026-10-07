"""Operator-facing output: the exact log lines and the run summary."""

import sys


class Log:
    """Prints `[e2e <platform>] ...` lines and mirrors them into run.log once a run directory exists."""

    def __init__(self, platform, stream=None):
        self.prefix = "[e2e %s] " % platform
        self.stream = stream or sys.stdout
        self.lines = []
        self.file = None

    def attach(self, path):
        self.file = open(path, "a")
        for line in self.lines:
            self.file.write(line + "\n")
        self.file.flush()

    def close(self):
        if self.file:
            self.file.close()
            self.file = None

    def say(self, text):
        line = self.prefix + text
        self.lines.append(line)
        self.stream.write(line + "\n")
        self.stream.flush()
        if self.file:
            self.file.write(line + "\n")
            self.file.flush()

    def scope(self, total, green, to_run, previously_failed, fp):
        self.say("scope: %d scenarios; %d already green for fp %s; %d to run (%d previously failed, %d pending)."
            % (total, green, fp[:12], to_run, previously_failed, to_run - previously_failed))

    def start(self, index, total, scenario, attempt, cap):
        self.say("START %d/%d %s attempt %d/%d (timeout %ds)" % (index, total, scenario.id, attempt, cap, scenario.timeout))

    def passed(self, scenario, attempt, seconds):
        self.say("PASS  %s attempt %d in %ds" % (scenario.id, attempt, round(seconds)))

    def failed(self, scenario, attempt, seconds, klass, summary):
        self.say("FAIL  %s attempt %d in %ds class=%s: %s" % (scenario.id, attempt, round(seconds), klass, summary))

    def retry(self, scenario, attempt, cap):
        self.say("RETRY %s: rerunning only this scenario (attempt %d of %d). No other scenario is rerun."
            % (scenario.id, attempt, cap))

    def stop(self, scenario, text):
        self.say("STOP  %s %s" % (scenario.id, text))

    def result(self, outcome, green, total, failed, not_run, exit_code, fp):
        failures = ", ".join("%s (%s)" % item for item in failed) or "none"
        self.say("RESULT %s: %d/%d green for fp %s; failed: %s; not run: %d; exit %d."
            % (outcome, green, total, fp[:12], failures, not_run, exit_code))


DIAGNOSIS = {
    "typed_text_mismatch": "The text that reached the app or the backend differs from what was typed. This is a product or "
        "driver defect, never load. It is not retried and rerunning does not diagnose it.",
    "app_crash": "The app crashed or stopped responding. This is a product defect. It is not retried; read crash.txt.",
}


def same_class_diagnosis(klass):
    return "It failed twice with class %s. Another retry is not a remedy; read the attempt artifacts." % klass


def write_summary(path, lines):
    with open(path, "w") as handle:
        handle.write("\n".join(lines) + "\n")
