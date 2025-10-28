import java.util.concurrent.*;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new IllegalArgumentException("Request limit must be positive");
        }

        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }


    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
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

        public boolean isImportRequest() { return importRequest; }
        public void setImportRequest(boolean importRequest) { this.importRequest = importRequest; }

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

    private static class DocumentSerializer {
        public static String toJson(Document document) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");

            sb.append("\"description\":{");
            sb.append("\"participantInn\":\"").append(escapeJson(document.getDescription().getParticipantInn())).append("\"");
            sb.append("},");

            appendField(sb, "doc_id", document.getDoc_id(), true);
            appendField(sb, "doc_status", document.getDoc_status(), true);
            appendField(sb, "doc_type", document.getDoc_type(), true);
            appendField(sb, "importRequest", String.valueOf(document.isImportRequest()), false);
            appendField(sb, "owner_inn", document.getOwner_inn(), true);
            appendField(sb, "participant_inn", document.getParticipant_inn(), true);
            appendField(sb, "producer_inn", document.getProducer_inn(), true);
            appendField(sb, "production_date", document.getProduction_date(), true);
            appendField(sb, "production_type", document.getProduction_type(), true);

            sb.append("\"products\":[");
            if (document.getProducts() != null) {
                for (int i = 0; i < document.getProducts().length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(productToJson(document.getProducts()[i]));
                }
            }
            sb.append("],");

            appendField(sb, "reg_date", document.getReg_date(), true);
            appendField(sb, "reg_number", document.getReg_number(), false);

            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }

            sb.append("}");
            return sb.toString();
        }

        private static String productToJson(Product product) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");

            appendField(sb, "certificate_document", product.getCertificate_document(), true);
            appendField(sb, "certificate_document_date", product.getCertificate_document_date(), true);
            appendField(sb, "certificate_document_number", product.getCertificate_document_number(), true);
            appendField(sb, "owner_inn", product.getOwner_inn(), true);
            appendField(sb, "producer_inn", product.getProducer_inn(), true);
            appendField(sb, "production_date", product.getProduction_date(), true);
            appendField(sb, "tnved_code", product.getTnved_code(), true);
            appendField(sb, "uit_code", product.getUit_code(), true);
            appendField(sb, "uitu_code", product.getUitu_code(), false);

            if (sb.charAt(sb.length() - 1) == ',') {
                sb.setLength(sb.length() - 1);
            }

            sb.append("}");
            return sb.toString();
        }

        private static void appendField(StringBuilder sb, String name, String value, boolean comma) {
            if (value != null) {
                sb.append("\"").append(name).append("\":\"")
                        .append(escapeJson(value)).append("\"");
                if (comma) sb.append(",");
            }
        }

        private static String escapeJson(String str) {
            if (str == null) return "";
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);

        Product product1 = new Product("cert1", "2023-07-27", "123", "owner1",
                "producer1", "2023-07-27", "tnved1", "uit1", "uitu1");
        Product product2 = new Product("cert2", "2023-07-28", "456", "owner2",
                "producer2", "2023-07-28", "tnved2", "uit2", "uitu2");

        Product[] products = new Product[]{product1, product2};

        Document document = new Document("doc123", "approved", "LP_INTRODUCE_GOODS",
                "770123456789", "770123456789", "770987654321",
                "2023-07-27", "OWN_PRODUCTION", products, "2023-07-27");

        document.setImportRequest(true);
        document.setReg_number("reg123");

    }
}