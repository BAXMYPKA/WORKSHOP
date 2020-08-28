# WORKSHOP

### В ОБЩЕМ
Это - демо-проект универсального приложения для любой мастерской по предоставлению услуг.

В один самозапускающийся файл интегрированы: веб-интерфейс, база данных и приложение.

Дизайн страниц чисто мой без заимствований, шаблонов и шаблонизаторов. А поскольку я не дизайнер - то и огрехи все мои )))

### ВКРАТЦЕ О САМОМ ПРИЛОЖЕНИИ

Прием заказов, формирование набора услуг в заказе, назначение исполнителей, сопровождение и сигнализация пользователям о готовности.

## КАК ЗАПУСКАТЬ ПРИЛОЖЕНИЕ

Приложение представлено самозапускающимся архивом ~55MB [applicationWORKSHOP.jar](https://github.com/BAXMYPKA/WORKSHOP/raw/jar/applicationWORKSHOP.jar). Его можно скачать и запустить у себя локально.

Для запуска необходима предустановленная [Java SE 8+](https://www.oracle.com/java/technologies/javase-downloads.html)

После запуска заходим в любом браузере (Chrome, Mozilla, Edge) по адресу: [http://localhost:18080/workshop.pro/index-demo](http://localhost:18080/workshop.pro/index-demo)
 
Выключается кнопкой "ЗАКРЫТЬ" в верхнем правом углу интерфейса. (В противном случае, вам придется искать java-процесс в памяти и убивать его вручную.) После выключения приложение гасит базу данных и все изменения сгорают. Вам на память остаются только кука с языком кука JWT-токеном - обе с небольшим сроком жизни.
 
Интерфейс состоит из трех основных частей (это три меню слева):
 
 * Главная демо-страница с кратким описанием проекта.
 * Главная страница проекта для клиентов. Все заявленные особенности честно и полноценно работают во взаимодействии с базой данных. 
 * Внутренний домен для сотрудников - это в разработке, планируется React.

При запуске стартуют:
 * Сервер Tomcat
 * SQL база данных в оперативной памяти, в которую предзагружаются пробные сущности всех видов (пользователи, сотрудники, заказы и т.п.).

## СИСТЕМНЫЕ ТРЕБОВАНИЯ

* Установленная Java JRE version 8 (1.8.0) и выше.
* Оперативной памяти хотя бы 4Гб
* Свободные TCP/IP порты:
    * 18080 для функционирования сервера. *Его можно изменить в файле "application.properties" в строке "server.port="*
    * 9092 *Его можно изменить в файле "application.properties" в строке "h2.port="*

### ВКРАТЦЕ ОБ УСТРОЙСТВЕ ПРИЛОЖЕНИЯ

Сам пакет приложения - все в одном (фронтенд и бэкенд). Внутри делится на отдельное HTML/CSS/JavaScript фронтенд-приложение, которое обращается к отдельному же Java бекэнду.

Проект многомодульный, каждый модуль отвечает за свою функциональность:

    application - Главный модуль со стартовой конфигурацией SpringBoot, стартовым классом и глобальными частями приложения.
    security -  Модуль с настройками безопасности для аутентификации и авторизации.
    internal - Основной внутренний REST-сервис, отвечающий за базовые сущности приложения и их дистрибуцию.
                Содержит в себе всю базовую логику приложения.
                Больше ответственнен за рабочий процесс сотрудников условной организации. 
    external - MVC-Web сервис, смотрящий наружу для клиентов организации.
                Основной сайт условной организации,услуги, цены, отслеживание выполнения заказов и т.п.
    sharedResources - html страницы, css стили, javascript скрипты, *.properties и языковые файлы, общие для всех модулей приложения.

## ИСПОЛЬЗОВАННЫЕ ТЕХНОЛОГИИ

* **Maven.** 
* **Spring Boot.** 
* **Spring Security + JWT (JSON Web Token).** 
* **Spring MVC / Thymeleaf / HTML+CSS.** 
* **Spring HATEOAS (REST).** 
* **JPA (Hibernate).**
* **SQL (H2 database).** 
* **Hibernate Validation, Jackson JSON, Lombok etc...**
* **JavaScript.** 
* **React.**
* **JUnit / Mockito**

## КРАТКО О ИСПОЛЬЗУЕМОЙ ФУНКЦИОНАЛЬНОСТИ

В качестве REST-приложения, принимает на вход те или иные сущности в виде JSON-объектов, на запросы отвечает такими же сущностями в виде JSON-объектов.

Каждая сущность (или коллекция таковых) содержит в себе набор ссылок для управления - как сама сущность, так и все вложенные в нее.

Коллекции сущностей или коллекции вложенных сущностей выдаются постранично со ссылками на текущую страницу, предыдущую, следующую или последнюю (если таковые имеются). Размер страниц и количество сущностей на каждую задаются в параметрах запроса.

Сортировать можно по проивольному свойству в алфавитном или обратном порядке.

Например, на GET-запрос всех Positions (Должностей) принадлежащих объекту Department (Отдел) с id=300 по ссылке вида [workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=1&order-by=name](http://localhost:18080/workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=1&order-by=name&order=asc) вернется HTTP-response с заголовком:
 
        Content-Type:"application/hal+json;charset=UTF-8"
 
Поскольку Positions, прикрепленных к данному Department, может быть много, то они будут выдаваться постранично. В данном случае, на странице номер 1 будет 3 Positions, сортированных по полю "name" по возрастанию.

А тело ответа в виде JSON, в начале которого идет список ссылок для выборки всех Positions:

    ```json 
    {
        "links": [{
                "rel": "currentPage",
                "href": "http://localhost/workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=4&order-by=name&order=DESC",
                "hreflang": "en",
                "media": "application/json; charset=utf-8",
                "title": "Page 1 of 4 pages total. Elements 3 of 10 elements total."
            }, {
                "rel": "nextPage",
                "href": "http://localhost/workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=2&order-by=name&order=DESC",
                "hreflang": "en",
                "media": "application/json; charset=utf-8",
                "title": "Position"
            }, {
                "rel": "lastPage",
                "href": "http://localhost/workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=5&order-by=name&order=DESC",
                "hreflang": "en",
                "media": "application/json; charset=utf-8",
                "title": "Page 4"
            }
        ],

Затем в свойстве "content" идут сами Positions (начинаются со свойства "identifier"):

            "content": [{
                    "identifier": 159,
                    "created": "2019-10-10T16:37:14.386+03:00",
                    "name": "Position unique 10",
                    "department": {
                        "@id": "9711e8f1-069a-4c1c-9980-4d2d5e14bd0d",
                        "identifier": 300,
                        "name": "Department for paginated position",
                        "created": "2019-10-10T16:37:14.282+03:00"
                    },
                    "workshopEntityName": "Position",
                    "links": [{
                            "rel": "self",
                            "href": "http://localhost/workshop.pro/internal/positions/159",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "Position"
                        }, {
                            "rel": "PositionEmployees",
                            "href": "http://localhost/workshop.pro/internal/positions/159/employees",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "[/{id}/employees],[GET,POST,PUT,DELETE]"
                        }, {
                            "rel": "PositionInternalAuthorities",
                            "href": "http://localhost/workshop.pro/internal/positions/159/internal-authorities",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "[/{id}/internal-authorities],[GET,POST,PUT,DELETE]"
                        }, {
                            "rel": "PositionDepartment",
                            "href": "http://localhost/workshop.pro/internal/positions/159/department",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "[/{id}/department],[GET,POST,PUT,DELETE]"
                        }
                    ]
                }, {
                    "identifier": 158,
                    "created": "2019-10-10T16:37:14.386+03:00",
                    "name": "Position unique 09",
                    "department": "9711e8f1-069a-4c1c-9980-4d2d5e14bd0d",
                    "workshopEntityName": "Position",
                    "links": [{
                            "rel": "self",
                            "href": "http://localhost/workshop.pro/internal/positions/158",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "Position"
                        }, {
                            "rel": "PositionEmployees",
                            "href": "http://localhost/workshop.pro/internal/positions/158/employees",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "[/{id}/employees],[GET,POST,PUT,DELETE]"
                        }, {
                            "rel": "PositionInternalAuthorities",
                            "href": "http://localhost/workshop.pro/internal/positions/158/internal-authorities",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "[/{id}/internal-authorities],[GET,POST,PUT,DELETE]"
                        }, {
                            "rel": "PositionDepartment",
                            "href": "http://localhost/workshop.pro/internal/positions/158/department",
                            "hreflang": "en",
                            "media": "application/json; charset=utf-8",
                            "title": "[/{id}/department],[GET,POST,PUT,DELETE]"
                        }
                    ]
                }, {
                ... omitted ...
 Каждая из Positions, в свою очередь, содержит ссылки:
 
 * На саму себя (relation="self"). По этому адресу можно запросить (протоколом GET), изменить (протоколом PUT) или удалить (протоколом DELETE) нужную Position, если позволяют права доступа.
 * На список сотрудников, находящихся в данной Должности (relation="PositionEmployees"). По этому же адресу теми же стандартными протоколами можно добавить, править или удалить сотрудника с "identifier"=100 из должности по ссылке: [workshop.pro/internal/positions/159/employees/100](workshop.pro/internal/positions/159/employees/100)
 * На список прав доступа для этой Должности (relation="PositionInternalAuthorities")
 * И непосредственно на сам Department, которому эта Position принадлежит (relation="PositionDepartment").
 
 Если, в соответствии с правами доступа, пользователю, запросившему данные Positions, нельзя видеть подробности сущностей Department, то вложенное свойство "department" будет содержать только 'identifier' и 'name'.

## БЕЗОПАСНОСТЬ (АУТЕНТИФИКАЦИЯ И АВТОРИЗАЦИЯ)

В данном демо-режиме все работает без HTTPS и передается открыто. Но! Работает честно и по полной программе.

Процессы проверки пользователей и их доступов вкратце расписаны в файле [SECURITY_FLOW](https://github.com/BAXMYPKA/WORKSHOP/blob/master/SECURITY_FLOW.md)

## ЧТО В ПРОЦЕССЕ (И ДАЖЕ ФУНКЦИОНИРУЕТ!)

* Фронтэнд для внешнего приложения: демо-страница с описанием проекта, главная страница с полнофункциональной возможностью входа, управлением профилем, заказами, тестовыми данными и проч.

## ЧТО ЕЩЕ НЕ СДЕЛАНО, НО В ПРОЦЕССЕ

* Фронтэнд для внутреннего сервиса - это отдельное React-приложение, активно взаимодействующее с Java REST-сервисом бекэнда. В процессе активной разработки, но туда уже можно заглянуть и увидеть немного интерактивный скелет будущего функционала.

* В JavaScript скриптах пока местами только русский текст вместо адаптивной интернационализации.
* Телефонный сервис. Таковой должен сигнализировать о готовности заказа клиентам.

## КАК ПРИЛОЖЕНИЕ ДОЛЖНО РАБОТАТЬ В ИДЕАЛЬНОМ МИРЕ

*Приложение пока не слишком функционально, т.к. должно управляться через JavaScript интерфейс, который показывает все возможности,
но полностью способно обрабатывать REST-запросы всех объявленных видов для всех сущностей.*

Допустим, у нас веломастерская, предоставляющая услуги по ремонту и обслуживанию велосипедов.

При оформлении заказа менеджером:

* Из списка доступных Классификаторов (Classifiers) с фиксированной ценой выбирается набор услуг.

Например, заднее колесо требует протяжки спиц, замены эксцентрика и закраску царапины. Протяжка и эксцентрик как услуги присутствуют в публичном прайсе, а закраску цапапины можно сделать бесплатно в подарок: создаем кастомную услугу с описанием и нулевой ценой.

* Теперь все эти три услуги составляют одну Задачу (Task) для заднего колеса.

Задача суммирует всю стоимость услуг и желаемое время окончания работ. После создания задачи можно сразу назначить на нее исполнителя (Employee) или он сам в будущем может назначить ее на себя сам.

* Все задачи собираются в один Заказ (Order).

Тут суммируются вся стоимость Задач, желаемое время окончания всех работ и Клиент.
 
* Для контакта с клиентом обязательно заносятся либо email, либо телефон клиента (User).

После того, как Исполнитель (Employee
) завершит последнюю Задачу или в Заказе будет выставлено поле "finished", клиенту высылается email или СМС на указанный телефон.

В процессе выполнения заказа Клиент может отслеживать его на сайте по выданному ID.

*Также внутри содержится немного внутренней кухни, как то, Департаменты, Отделы, Должности и разрешения для сотрудников Отдела Кадров (HR) по управлению персоналом.* 
