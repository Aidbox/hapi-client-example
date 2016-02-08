import static org.junit.Assert.assertEquals;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import org.junit.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;

public class PatientTest {
    @Test
    public void testPatient() {
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

        Patient patient = new Patient();
        patient.addIdentifier().setSystem("urn:system").setValue("value-aidbox");
        patient.addName().addFamily("Aidbox").addGiven("John");

        MethodOutcome outcome = client.create()
                .resource(patient)
                .conditional()
                .where(Patient.IDENTIFIER.exactly().systemAndIdentifier("urn:system", "value-aidbox"))
                .execute();

        Boolean created = outcome.getCreated();

        IdDt id = (IdDt) outcome.getId();

        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("Aidbox"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        assertEquals("Must be 1 patient named 'Aidbox'", 1, results.getEntry().size());
    }
}