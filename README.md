# Autostattic [![Build statistics](https://github.com/wzieba/Autostattic/actions/workflows/main.yml/badge.svg?event=schedule)](https://github.com/wzieba/Autostattic/actions/workflows/main.yml)

Autostattic is a tool for observing metrics of open-source Android projects. It uses [Github Actions workflow](https://github.com/wzieba/Autostattic/blob/main/.github/workflows/main.yml)
run periodically to execute set of tasks and saves results in csv files in this repository. Then, those results are used in Google Sheets to display values
and draw charts.

Currently it supports measuring % of outdated dependencies in following projects:
- WooCommerce
- WordPress
- Simplenote
- FluxC

[Please visit the spreadsheet to see results.](https://docs.google.com/spreadsheets/d/1LCRikcsuB8Kr0nojEcOdk_1_90IPHhIOd8nAgyI7OFk/edit?usp=sharing)


## Roadmap

- [ ] Number of Kotlin compiler warnings
- [ ] [gradle-profiler](https://github.com/gradle/gradle-profiler) based build-time
- [ ] Detekt baseline size
- [ ] Jetifier removal possibility
