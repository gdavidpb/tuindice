## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- The `graphify` CLI is NOT on PATH. Invoke it as `~/.local/bin/graphify` or run `export PATH="$HOME/.local/bin:$PATH"` first.
- Use `graphify query "<question>"` for architecture questions or when you do not know where something lives; `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. They return a scoped subgraph, smaller than GRAPH_REPORT.md or a broad grep. A search for a known file, symbol or string goes straight to grep or Read: the graph is AST-only and lags uncommitted work.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- Run `graphify update .` after a batch of code changes that you or a subagent will explore with the graph next (AST-only, no API cost); not after every edit.
