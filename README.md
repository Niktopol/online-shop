# Курсовая работа: Создание системы управления инвентарем и заказами для небольшого онлайн-магазина

Этот проект представляет собой курсовую работу по предмету "Архитектура клиент-серверных приложений", в рамках которой было разработано приложение и API небольшого интернет-магазина.

## Как запустить приложение

1. **Склонируйте репозиторий:**

    ```sh
    git clone https://github.com/Niktopol/online-shop.git
    ```

2. **Перейдите в директорию проекта:**

    ```sh
    cd online-shop
    ```

3. **Запустите Docker-контейнеры с помощью Docker Compose:**

    ```sh
    docker-compose up --build
    ```

## Стек технологий

- Java 17
- Spring Boot
- Spring Session
- Spring Data JPA
- Spring Security
- Spring MVC
- PostgreSQL
- GraphQL
- gRPC
- Docker
- Gradle

Это приложение разработано с использованием Java 17 и фреймворка Spring. Проект состоит из трёх серисов, связанных посредством gRPC. Для работы с базой данных используется Spring Data JPA, а аутентификация и авторизация пользователей обеспечивается с помощью Spring Security. Реализована система ролей пользователей: **OWNER** и **CUSTOMER**. В проекте испольуется база данных PostgreSQL. Для управления зависимостями и сборкой проекта используется Gradle. Приложение также контейнизировано с использованием Docker.