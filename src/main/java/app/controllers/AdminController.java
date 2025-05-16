package app.controllers;

import app.DTO.AdminDTO;

import app.entities.Product;
import app.entities.ProductVariant;
import app.exceptions.DatabaseException;
import app.persistence.*;
import app.util.PasswordUtil;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import app.persistence.CustomerMapper;
import java.sql.SQLException;
import java.util.List;


public class AdminController {

    private final AdminMapper adminMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;

    public AdminController(AdminMapper adminMapper, CustomerMapper customerMapper, ProductMapper productMapper) {
        this.adminMapper = adminMapper;
        this.customerMapper = customerMapper;
        this.productMapper = productMapper;
    }

    public void adminLogin(@NotNull Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password"); // Plain password

        try {
            // checks the hashing code
            AdminDTO adminDTO = adminMapper.login(email, password);
            // success with login
            ctx.sessionAttribute("currentAdmin", adminDTO); // Use a consistent session key
            // Redirect to the admin dashboard
            ctx.redirect("/admin/dashboard");

        } catch (DatabaseException e) {
            // Handle known login errors (email not found, wrong password)
            ctx.attribute("message", e.getMessage());
            ctx.render("admin_login.html"); //
        } catch (SQLException e) {
            System.err.println("SQL Exception during admin login: " + e.getMessage());
            ctx.attribute("message", "En databasefejl opstod");
            ctx.render("admin_login.html");
        }
    }

    public void createAdmin(@NotNull Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");
        String passwordConfirm = ctx.formParam("password2");

        if (password == null || password.isEmpty() || !password.equals(passwordConfirm)) {
            ctx.attribute("message", "Kodeordene matcher ikke");
            ctx.render("admin/create_admin.html");
            return;
        }
        String hashedPassword = PasswordUtil.hashPassword(password);
        try {
            adminMapper.createAdmin(email, hashedPassword);
            ctx.attribute("message", "Admin oprettet: " + email);
            // Redirect to dashboard
            ctx.redirect("/admin/dashboard");

        } catch (DatabaseException e) {
            // Handle errors from mapper (e.g., duplicate email)
            ctx.attribute("message", e.getMessage());
            ctx.render("admin/create_admin.html");
        }
    }

    public void handleCreateProduct(Context ctx) {
        try {
            // 1. Læs produktdata fra formularen
            String name = ctx.formParam("name");
            String unit = ctx.formParam("unit");
            int price = Integer.parseInt(ctx.formParam("price"));

            // 2. Opret produktobjekt
            Product product = new Product(name, unit, price);

            // 3. Gem produkt i DB og få ID
            int productId = productMapper.insertProduct(product);
            product.setProductId(productId); // så vi kan sende det til ProductVariant

            // 4. Læs varianter (format: længde-bredde, fx "480-45,600-45")
            String[] variants = ctx.formParam("variants").split(",");

            for (String v : variants) {
                String[] dims = v.trim().split("-"); // fx ["480", "45"]
                int length = Integer.parseInt(dims[0].trim());
                int width = Integer.parseInt(dims[1].trim());

                ProductVariant variant = new ProductVariant(length, width, product);
                System.out.println("➡ Variant: Længde = " + length + ", Bredde = " + width);
                productMapper.insertProductVariant(variant, productId);
            }

            ctx.redirect("/admin/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fejl ved oprettelse af produkt: " + e.getMessage());
        }
    }

    public void showAllProducts(Context ctx) {
        try {
            List<Product> products = productMapper.getAllProducts();
            ctx.attribute("products", products);
            ctx.render("admin_products.html");
        } catch (DatabaseException e) {
            ctx.status(500).result("Fejl ved visning af produkter: " + e.getMessage());
        }
    }

    public void handleUpdateProductPrices(Context ctx) {
        try {
            List<Product> products = productMapper.getAllProducts();

            for (Product product : products) {
                String param = ctx.formParam("price_" + product.getProductId());
                if (param != null) {
                    int newPrice = Integer.parseInt(param);
                    productMapper.updateProductPrice(product.getProductId(), newPrice);
                }
            }

            ctx.redirect("/admin/products");

        } catch (Exception e) {
            ctx.status(500).result("Fejl ved opdatering af priser: " + e.getMessage());
        }
    }

    public void handleDeleteProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            productMapper.deleteProductAndVariants(productId);
            ctx.redirect("/admin/products");
        } catch (Exception e) {
            ctx.status(500).result("Fejl ved sletning af produkt: " + e.getMessage());
        }
    }

}