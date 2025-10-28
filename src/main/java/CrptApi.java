import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Semaphore semaphore;
    private final long delayMillis;
    private final String url = "https://ismp.crpt.ru/api/v3/";


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Request limit must be positive");
        }

        this.delayMillis = timeUnit.toMillis(1) / requestLimit;
        this.semaphore = new Semaphore(requestLimit);

        this.httpClient = HttpClients.createDefault();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        startPermitRefiller(requestLimit);
    }

    public void createDocument(Document document, String signature) {
        try {
            semaphore.acquire();

            String requestBody = objectMapper.writeValueAsString(document);

            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Signature", signature);
            request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

            HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 400) {
                String responseBody = EntityUtils.toString(response.getEntity());
                throw new ApiException("API request failed with status: " + statusCode + ". Response: " + responseBody);
            }

            EntityUtils.consume(response.getEntity());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Interrupted while waiting for rate limit", e);
        } catch (JsonProcessingException e) {
            throw new ApiException("Failed to serialize document to JSON", e);
        } catch (IOException e) {
            throw new ApiException("Failed to execute HTTP request", e);
        }
    }

    private void startPermitRefiller(int requestLimit) {
        Thread refillThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(delayMillis);
                    if (semaphore.availablePermits() < requestLimit) {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        refillThread.setDaemon(true);
        refillThread.start();
    }

    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private Boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private Product[] products;
        private String reg_date;
        private String reg_number;

        public Document(String doc_id, String doc_status, String doc_type, String owner_inn,
                        String participant_inn, String producer_inn, String production_date,
                        String production_type, Product[] products, String reg_date) {
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.description = new Description(participant_inn);
        }

        public Description getDescription() { return description; }
        public void setDescription(Description description) { this.description = description; }

        public String getDoc_id() { return doc_id; }
        public void setDoc_id(String doc_id) { this.doc_id = doc_id; }

        public String getDoc_status() { return doc_status; }
        public void setDoc_status(String doc_status) { this.doc_status = doc_status; }

        public String getDoc_type() { return doc_type; }
        public void setDoc_type(String doc_type) { this.doc_type = doc_type; }

        public Boolean getImportRequest() { return importRequest; }
        public void setImportRequest(Boolean importRequest) { this.importRequest = importRequest; }

        public String getOwner_inn() { return owner_inn; }
        public void setOwner_inn(String owner_inn) { this.owner_inn = owner_inn; }

        public String getParticipant_inn() { return participant_inn; }
        public void setParticipant_inn(String participant_inn) { this.participant_inn = participant_inn; }

        public String getProducer_inn() { return producer_inn; }
        public void setProducer_inn(String producer_inn) { this.producer_inn = producer_inn; }

        public String getProduction_date() { return production_date; }
        public void setProduction_date(String production_date) { this.production_date = production_date; }

        public String getProduction_type() { return production_type; }
        public void setProduction_type(String production_type) { this.production_type = production_type; }

        public Product[] getProducts() { return products; }
        public void setProducts(Product[] products) { this.products = products; }

        public String getReg_date() { return reg_date; }
        public void setReg_date(String reg_date) { this.reg_date = reg_date; }

        public String getReg_number() { return reg_number; }
        public void setReg_number(String reg_number) { this.reg_number = reg_number; }
    }

    public static class Description {
        private String participantInn;

        public Description() {}

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getParticipantInn() { return participantInn; }
        public void setParticipantInn(String participantInn) { this.participantInn = participantInn; }
    }

    public static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;

        public Product(String owner_inn, String producer_inn, String production_date, String tnved_code) {
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
        }

        public Product(String certificate_document, String certificate_document_date,
                       String certificate_document_number, String owner_inn, String producer_inn,
                       String production_date, String tnved_code, String uit_code, String uitu_code) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = certificate_document_date;
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }

        public String getCertificate_document() { return certificate_document; }
        public void setCertificate_document(String certificate_document) { this.certificate_document = certificate_document; }

        public String getCertificate_document_date() { return certificate_document_date; }
        public void setCertificate_document_date(String certificate_document_date) { this.certificate_document_date = certificate_document_date; }

        public String getCertificate_document_number() { return certificate_document_number; }
        public void setCertificate_document_number(String certificate_document_number) { this.certificate_document_number = certificate_document_number; }

        public String getOwner_inn() { return owner_inn; }
        public void setOwner_inn(String owner_inn) { this.owner_inn = owner_inn; }

        public String getProducer_inn() { return producer_inn; }
        public void setProducer_inn(String producer_inn) { this.producer_inn = producer_inn; }

        public String getProduction_date() { return production_date; }
        public void setProduction_date(String production_date) { this.production_date = production_date; }

        public String getTnved_code() { return tnved_code; }
        public void setTnved_code(String tnved_code) { this.tnved_code = tnved_code; }

        public String getUit_code() { return uit_code; }
        public void setUit_code(String uit_code) { this.uit_code = uit_code; }

        public String getUitu_code() { return uitu_code; }
        public void setUitu_code(String uitu_code) { this.uitu_code = uitu_code; }
    }

    public static class ApiException extends RuntimeException {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);

        Product product1 = new Product("CONFORMITY_CERTIFICATE", "2023-07-27", "123", "770123456789",
                "770987654321", "2023-07-27", "1234567890", "uit123456", "uitu123456");
        Product product2 = new Product("CONFORMITY_DECLARATION", "2023-07-28", "456", "770123456789",
                "770987654321", "2023-07-28", "0987654321", "uit654321", "uitu654321");

        Product[] products = new Product[]{product1, product2};

        Document document = new Document("doc123", "IN_PROGRESS", "LP_INTRODUCE_GOODS",
                "770123456789", "770123456789", "770987654321",
                "2023-07-27", "OWN_PRODUCTION", products,
                "2025-12-12");

        document.setImportRequest(false);
        document.setReg_number("REG-" + System.currentTimeMillis());

        String signature = "signature_base64";

        try {
            crptApi.createDocument(document, signature);
        } catch (ApiException e) {
            System.err.println( e.getMessage());
            e.printStackTrace();
        }
    }
}