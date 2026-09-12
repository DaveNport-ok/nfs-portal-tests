# NFS Portal Tests

[![UI Tests](https://github.com/DaveNport-ok/nfs-portal-tests/actions/workflows/tests.yml/badge.svg)](https://github.com/DaveNport-ok/nfs-portal-tests/actions/workflows/tests.yml)

Набор автотестов для сайта [NFS Portal](https://davenport-ok.github.io/nfs-portal).

## Стек
* **Язык:** Java
* **Инструменты:** Selenide, JUnit 5, Maven, Allure Report
* **Браузер:** Google Chrome

## Что проверяют тесты
* Загрузку и заголовок главной страницы.
* Доступность кнопок и навигационных ссылок.
* Открытие модального окна авторизации.
* Наличие атрибутов локализации (`data-i18n`).
* Отсутствие критических JS-ошибок в консоли браузера.

## Запуск тестов

Выполнение тестов в терминале:
```bash
mvn test
```

Генерация и открытие отчёта Allure:
```bash
mvn allure:serve
```

## Отчёт Allure Report

![Allure Report](https://github.com/user-attachments/assets/618ee88d-bc80-4e61-9bdd-9fa6d8b46926)
