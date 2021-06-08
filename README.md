# Beer stock

Uma api rest contruida com o intuito de demonstrar como utilizar teste no Spring boot e tambem demonstrou o funcionamento do TDD

Este projeto foi construido em lab na plataforma da [Digital innovation one](https://digitalinnovation.one/)

No projeto foi usada as bibliotecas de testes do Junit, Mockito e do Hamcrest todas devidamente ja veem instalados nas dependencias do spring boot.

Para testar os endpoints das api foi usado o spring test, enquanto para testar a classe de serviço foi usado o Mockito para mockar dependencias da classe de serviço como os repositorios, depois foi utilizado os metodos do hamcrest para fazer as comparações do que foi mandado com o que é retornado
