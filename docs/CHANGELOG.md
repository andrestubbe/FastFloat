# FastFloat Changelog

## [0.1.1] - 2026-08-14
- Integrated native `FastSIMD` (v0.1.3) AVX2 256-bit vector parsing engine.
- Added official JMH benchmark suite measuring 21.65 Million float parsing ops/sec (5.1× speedup vs Java).
- Added `Real-World Use Cases` and `Performance Benchmarks` documentation sections.
- Removed deprecated `Project Structure` section from README.md.
- Updated full 5-module dependency stack (`FastFloat`, `FastSIMD`, `FastMemory`, `FastPointer`, `FastCore`).

## [0.1.0] - 2026-05-17
- Initial release of FastFloat with native C++ AVX2 parser and Ryu formatter.
