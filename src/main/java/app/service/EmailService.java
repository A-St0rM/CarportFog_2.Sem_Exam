package app.service;

import app.controllers.RoutingController;
import app.config.SessionConfig;
import app.config.ThymeleafConfig;

import app.entities.Customer;
import app.entities.Order;
import app.persistence.ConnectionPool;

import com.sendgrid.helpers.mail.objects.Personalization;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.io.IOException;


import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

public class EmailService {

    static String API_KEY = System.getenv("SENDGRID_API_KEY");

    public void sendOffer(Order order) throws IOException {

        Customer customer = order.getCustomer();

        Email from = new Email("Johannes@johannesfoog.dk");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(customer.getEmail()));
        personalization.addDynamicTemplateData("name", customer.getName());
        personalization.addDynamicTemplateData("email", customer.getEmail());
        personalization.addDynamicTemplateData("price", order.getTotalPrice());
        mail.addPersonalization(personalization);
        mail.setTemplateId("d-98633b54660e4e6c839007bc756debd9");

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("SendGrid Offer Response Status Code: " + response.getStatusCode());

        } catch (IOException ex) {
            System.err.println("Fejl ved afsendelse af tilbudsmail: " + ex.getMessage());
            throw ex; // sender fejlen videre
        }
    }

    public void sendConfirmation(Order order) throws IOException {
        Customer customer = order.getCustomer();
        Email from = new Email("Johannes@johannesfoog.dk");
        from.setName("Johannes Fog Byggemarked");

        Mail mail = new Mail();
        mail.setFrom(from);
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(customer.getEmail())); // Hent email fra customer i stedet for order
        personalization.addDynamicTemplateData("name", customer.getName()); // -||-
        personalization.addDynamicTemplateData("email", customer.getEmail()); // -||-
        personalization.addDynamicTemplateData("price", order.getTotalPrice());
        mail.addPersonalization(personalization);

        mail.setTemplateId("d-9ca13dba9799482ca8a989a26e4f92d8");

        SendGrid sg = new SendGrid(API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            System.out.println("SendGrid Confirmation Response Status Code: " + response.getStatusCode());

        } catch (IOException ex) {
            System.err.println("Fejl ved afsendelse af bekræftelsesmail: " + ex.getMessage());
            throw ex;
        }
    }
}
