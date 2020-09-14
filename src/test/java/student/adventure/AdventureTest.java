package student.adventure;

import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;


public class AdventureTest {
    @Before
    public void setUp() {
        // This is run before every test.
    }

    @Test
    public void sanityCheck() {
        // TODO: Remove this unnecessary test case.
        assertThat("CS 126: Software Design Studio", CoreMatchers.containsString("Software"));
    }
}