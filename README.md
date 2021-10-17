# Autostattic [![Build statistics](https://github.com/wzieba/Autostattic/actions/workflows/main.yml/badge.svg?event=schedule)](https://github.com/wzieba/Autostattic/actions/workflows/main.yml)

Autostattic is a tool for observing metrics of open-source Android projects. It uses [Github Actions workflow](https://github.com/wzieba/Autostattic/blob/main/.github/workflows/main.yml)
run periodically to execute set of tasks and saves results in csv files in this repository. Then, those results are used 
in [Autostattic Browser](https://github.com/wzieba/Autostattic-Browser) project to display values and draw charts. 

Currently it supports measuring % of outdated dependencies in following projects:
- [WooCommerce](https://github.com/woocommerce/woocommerce-android)
- [WordPress](https://github.com/wordpress-mobile/WordPress-Android)
- [Simplenote](https://github.com/Automattic/simplenote-android)
- [FluxC](https://github.com/wordpress-mobile/WordPress-FluxC-Android)
- [WCStoreApp](https://github.com/hichamboushaba/WCStoreApp)

## Please visit [Autostattic-Browser](https://wzieba.github.io/Autostattic-Browser/) for data visualisation.
