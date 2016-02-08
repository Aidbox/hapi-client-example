import static org.junit.Assert.assertEquals;

import org.junit.Test;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.IGenericClient;

public class PatientTest {
    @Test
    public void multiplicationOfZeroIntegersShouldReturnZero() {

        assertEquals("10 x 0 must be 0", 0, 0);
        assertEquals("0 x 10 must be 0", 0, 0);
        assertEquals("0 x 0 must be 0", 1, 1);
    }

    @Test
    public void testPatient() {
        System.out.println("fast");
        System.out.println("Hello, World");
        FhirContext ctx = FhirContext.forDstu2();
        String serverBase = "http://fhirtest.uhn.ca/baseDstu2";// "https://maksym.aidbox.io/fhir"; //

        IGenericClient client = ctx.newRestfulGenericClient(serverBase);

// Perform a search
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("duck"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        System.out.println("Found " + results.getEntry().size() + " patients named 'duck'");

        assertEquals("0 x 0 must be 0", 10, results.getEntry().size());
    }
}