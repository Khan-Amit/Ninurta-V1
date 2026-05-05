# CSFS Proof‑of‑Coherence Mining

## Not Proof‑of‑Work (No Energy Waste)

Mining in Ninurta‑V1 is **Proof‑of‑Coherence (PoCoh)**. The reward depends on how well a new slice fits into the circular ledger.

## Coherence Formula

`Coherence = α × desire + β × domain_entropy + γ × timestamp_alignment`

- α, β, γ: network parameters
- desire: user‑set (0.0–1.0)
- domain_entropy: how unpredictable the slice is
- timestamp_alignment: how close to real time

## Miner Reward

`Reward = base_reward × coherence`

Higher coherence → higher reward.

## Energy Efficiency

Mining is just **sequential writes** to the CSFS arena. A phone mining 24/7 consumes less battery than streaming video.

## No ASIC, No Pool Dominance

Because coherence depends on user‑defined desire and domain entropy, no specialised hardware can dominate. Mining is **democratic**.

## Simulated in Current Version

The Mining tab runs a simulated coherence miner. In the real implementation, it will read actual CSFS arena data.
