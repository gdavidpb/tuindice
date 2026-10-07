"""JUnit report with real counts: skipped means not run or quarantined, never a pass."""

import xml.etree.ElementTree as ET

from .classify import JUNIT_FAILURES


def write(path, platform, results):
    """`results` is a list of dicts: id, status (passed|failed|skipped), class, summary, seconds, producedByRunId."""
    counts = {"failures": 0, "errors": 0, "skipped": 0}
    suite = ET.Element("testsuite", {"name": "local-certification-suite.%s" % platform})
    total_time = 0.0
    for item in results:
        case = ET.SubElement(suite, "testcase", {
            "classname": "local-certification-suite.%s" % platform, "name": item["id"], "time": "%.3f" % item["seconds"]})
        total_time += item["seconds"]
        if item.get("producedByRunId"):
            props = ET.SubElement(case, "properties")
            ET.SubElement(props, "property", {"name": "producedByRunId", "value": item["producedByRunId"]})
        if item["status"] == "skipped":
            counts["skipped"] += 1
            ET.SubElement(case, "skipped", {"message": item.get("summary", "not run")})
        elif item["status"] == "failed":
            tag = "failure" if item.get("class") in JUNIT_FAILURES else "error"
            counts["failures" if tag == "failure" else "errors"] += 1
            node = ET.SubElement(case, tag, {"type": item.get("class", "unknown"), "message": item.get("summary", "")})
            node.text = item.get("summary", "")
    suite.set("tests", str(len(results)))
    suite.set("failures", str(counts["failures"]))
    suite.set("errors", str(counts["errors"]))
    suite.set("skipped", str(counts["skipped"]))
    suite.set("time", "%.3f" % total_time)
    ET.ElementTree(suite).write(path, encoding="utf-8", xml_declaration=True)
    return counts
