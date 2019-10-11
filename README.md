# WORKSHOP
Демо-проект универсального приложения для любой мастерской по предоставлению услуг.

Прием заказов, формирование набора услуг в заказе, назначение исполнителей, сопровождение и сигнализация пользователям о готовности.

Сам пакет приложения - все в одном (фронтенд и бэкенд). Внутри должен делиться на отдельное Java-script фронтенд-приложение, которое обращается отдельному Java бекэнд-приложению.

## СИСТЕМНЫЕ ТРЕБОВАНИЯ
* Установленная Java JRE version 8 (1.8.0) и выше.
* Оперативной памяти хотя бы 4Гб
* Желательно свободными TCP/IP порты:
    * 18080 для функционирования сервера. *Его можно изменить в файле "application.properties" в строке "server.port="*
    * 9092 *Его можно изменить в файле "application.properties" в строке "р2.port="*

//////////////УКАЗАТЬ ПУТЬ К ФАЙЛУ В АРХИВЕ 

## КАК ЗАПУСКАТЬ ПРИЛОЖЕНИЕ
Приложение представлено самозапускающимся архивом ///////.jar.
При запуске стартуют:
 * Сервер Tomcat
 * SQL база данных в оперативной памяти, в которую предзагружаются пробные сущности всех видов (пользователи, сотрудники, заказы и т.п.).
 * Само приложение, занимающее в памяти около /////////////тут расчитать!
 
 Далее базовый WEB-интерфейс доступен по адресу: [http://localhost:18080/workshop.pro](http://localhost:18080/workshop.pro)
 
 После выключения приложение гасит базу данных и все изменения сгорают. Вам на память остаются только кука с языком кука JWT-токеном - обе с небольшим сроком жизни.

## ИСПОЛЬЗОВАННЫЕ ТЕХНОЛОГИИ
* Maven. Управление зависимостями и сборка.

Проект многомодульный, каждый модуль отвечает за свою функциональность:

    application - Головной модуль со стартовой конфигурацией SpringBoot.
    security -  Модуль с настройками безопасности для аутентификации и авторизации.
    internal - Основной внутренний REST-сервис, отвечающий за базовые сущности приложения и их дистрибуцию.
        Содержит в себе всю базовую логику приложения.
        Больше ответственнен за рабочий процесс сотрудников условной организации. 
    external - MVC-Web сервис, смотрящий наружу для клиентов организации.
    sharedResources - Все html страницы, css, *.properties и языковые файлы, общие для всех модулей приложения.

* Spring Boot. Место сбора и конфигурации по умолчанию для всего зоопарка технологий.

Быстрый старт, зависимости правильных версий, упрощенная настройка, всроенный сервер.

* Spring Security + JWT (JSON Web Token). Проверка пользователей и паролей.

Приложение умеет выписывать токены формата JWT, а Spring Security занимается их проверкой через конфигурацию и фильтры.

* Spring MVC / Thymeleaf / HTML+CSS. Работа в режиме стандартного сервера с веб-страницами.

Стартовая страница, страницы логина/регистрации и прочие информационные страницы в демо-режиме.

* Spring HATEOAS (REST). Базовый бекэнд реализован в виде REST-приложения.

Возвращаемые через стандартные HTTP-запросы сущности содержат в себе набор ссылок для управления самими собой, запроса встроенных сущностей и управлением каждой встроенной сущностью.

* JPA (Hibernate). Стандарт де-факто для ORM.

За исключением настройки кэширования второго уровня в Hibernate, используются команды стандартного интерфейса JPA, а не нативные команды Hibernate.

* SQL (H2 database). База данных в памяти.
Релизована на языке Java, запускается вместе с приложением и не требует места на диске.

* Hibernate Validation, Jackson JSON, Lombok etc...

Вспомогательные технологии, про них особо писать не стоит.

## ТЕСТИРОВАНИЕ
* JUnit / Mockito

Набор юнит-тестов плюс набор интеграционных тестов.

Вторых больше по причине не запутанности доменной логики и большей необходимости тестирования в связке. Интеграционные тесты вынесены в головной Maven-модуль "application", т.к. им требуются все зависимости.

<Объясните как запустить автоматическое тестирование этой системы.
Сквозное тестирование
Объясните, что проверяют эти тесты и зачем они нужны.
Пример>

---------------------------

## КРАТКО О ИСПОЛЬЗУЕМОЙ ФУНКЦИОНАЛЬНОСТИ
В качестве REST-приложения, принимает на вход те или иные сущности в виде JSON-объектов, на запросы отвечает такими же сущностями в виде JSON-объектов.

Каждая сущность (или коллекция таковых) содержит в себе набор ссылок для управления - как сама сущность, так и все вложенные в нее.

Коллекции сущностей или коллекции вложенных сущностей выдаются постранично со ссылками на текущую страницу, предыдущую, следующую или последнюю (если таковые имеются). Размер страниц и количество сущностей на каждую задаются в параметрах запроса.

Сортировать можно по проивольному свойству в алфавитном или обратном порядке.

Например, на GET-запрос всех Positions (Должностей) принадлежащих объекту Department (Отдел) с id=300 по ссылке вида [workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=1&order-by=name](http://localhost:18080/workshop.pro/internal/departments/300/positions?pageSize=3&pageNum=1&order-by=name) вернется HTTP-response с заголовком:
 
        Content-Type:"application/hal+json;charset=UTF-8"
 
Поскольку Positions, прикрепленных к данному Department, может быть много, то они будут выдаваться постранично. Таким образом, тело ответа будет в виде JSON, в начале которого идет список ссылок для выборки всех Positions:

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

## ЧТО ЕЩЕ НЕ СДЕЛАНО
* Т.к. фронтэнд должен быть отдельным JavaScript приложением, активно взаимодействующим с Java REST-сервисом бекэнда, то вот фронта пока и нет. Его временно заменяют стандартные тестовые HTML MVC странички.
* Почтовый сервис. При готовности заказа должен сигнализировать о готовности клиенту на почту.
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

После того, как Исполнитель (Employee) завершит последнюю Задачу, клиенту высылается email или СМС на указанный телефон.
В процессе выполнения заказа Клиент может отслеживать его на сайте по выданному ID.

*Также внутри содержится немного внутренней кухни, как то, Департаменты, Отделы, Должности и разрешения для сотрудников Отдела Кадров (HR) по управлению персоналом.* 

## ДОКУМЕНТАЦИЯ

### БЛАГОДАРНОСТИ
Все было сделано при помощи такой-то матери, [StackOverFlow](https://stackoverflow.com/) и [Google](https://www.google.com/).

От саомучки спасибо всем бесплатным статьям по Java, Java-фреймворкам, HTTP и SQL в Интернете, написанным энтузиастами или продвигающими свои учебные ресурсы людьми.

### АВТОР
Контакты.