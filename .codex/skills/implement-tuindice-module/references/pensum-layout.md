# Pensum Graph Layout

Use this when generating or refreshing pensum JSON/Mongo data that includes node positions and edge points.

## Goal

Keep each subject in its canonical trimester column, while reducing visual crossings and making every pensum deterministic across runs. The frontend should consume persisted `x`, `y`, `width`, `height`, and `edge.points`; it should not recompute the canonical layout at render time.

## Technique

Use a constrained Sugiyama-style layered layout:

1. Treat each `term_id` as a fixed layer. Do not move subjects across trimesters.
2. Normalize term columns to a fixed width, currently `240.0`.
3. Center every node horizontally inside its layer:

```text
node.x = term.x + (term.width - node.width) / 2
```

4. Reorder nodes only inside each layer. Use deterministic left-to-right and right-to-left barycenter sweeps:
   - For a node, find connected neighbors in already-positioned adjacent/nearby layers.
   - Score the node by the average normalized vertical order of those neighbors.
   - Sort by score, then previous order, then id.
   - Run several sweeps and keep the order with the lowest crossing count.
5. Assign vertical positions from top to bottom with stable spacing:

```text
node.y = topPadding + rowIndex * rowGap
```

Current fixture values:

```text
topPadding = 120.0
rowGap = 172.0
termWidth = 240.0
canvasRightPadding = 128.0
```

6. Recompute `edge.points` after node positions are final. Use orthogonal routing from node borders:
   - Prefer right-edge to left-edge routes for cross-trimester edges.
   - Use top/bottom routes when vertical distance dominates.
   - Include intermediate turn points so the Compose canvas can draw rounded corners.
   - Keep points deterministic; do not depend on viewport state or zoom.

## Crossing Count

For validation, count pairwise crossings between edges connecting the same pair of layers after normalizing each edge direction left-to-right. This is a simple heuristic metric, not a complete graph drawing proof. If the count does not improve, still keep the canonical normalized positions if they make columns consistent.

## Data Contract

The final payload should still match the existing pensum response shape:

```json
{
  "nodes": [
    {
      "term_id": "T8",
      "x": 1680.0,
      "y": 292.0,
      "width": 172.0,
      "height": 144.0
    }
  ],
  "edges": [
    {
      "from_node_id": "ci3311",
      "to_node_id": "ci3715",
      "points": [
        { "x": 1852.0, "y": 352.0 },
        { "x": 1920.0, "y": 352.0 }
      ]
    }
  ]
}
```

## Practical Rules

- Keep output deterministic: no random seed unless it is fixed and documented.
- Preserve `total_credits`; validate it after layout changes.
- Validate every edge references existing node ids.
- Keep slot fulfillment rules intact when moving nodes.
- If future pensums include many long edges, consider dummy nodes internally during crossing minimization, but do not persist dummy nodes in the API payload.
