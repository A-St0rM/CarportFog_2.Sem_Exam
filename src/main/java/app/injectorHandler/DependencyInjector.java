package app.injectorHandler;

import app.persistence.ConnectionPool;

public class DependencyInjector {
    private final ConnectionPool connectionPool;


    // Mappers

    // Services

    // Controllers

    public DependencyInjector(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;

        // Initialiser alle klasser Eksempel
//        this.cupcakeBottomMapper = new CupcakeBottomMapper(connectionPool);
//        this.cupcakeTopMapper = new CupcakeTopMapper(connectionPool);
//        this.cupcakeMapper = new CupcakeMapper(connectionPool);
//
//
//        this.cupcakeService = new CupcakeService(cupcakeBottomMapper, cupcakeTopMapper, cupcakeMapper, orderlineMapper, orderMapper, statusMapper);
//        this.orderlineService = new OrderlineService(cupcakeMapper, orderlineMapper, orderMapper, statusMapper, customerMapper);
//        this.cupcakeController = new CupcakeController(cupcakeService, cupcakeMapper, cupcakeTopMapper, cupcakeBottomMapper, orderlineService);

    }
}
