import name.velikodniy.vitaliy.fixedlength.FixedLength;
import name.velikodniy.vitaliy.fixedlength.FixedLengthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParserTest {

    String singleTypeExample =
            "Joe1      Smith     Developer 07500010012009\n" +
            "Joe3      Smith     Developer ";

    String mixedTypesExample =
            "EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "CatNoBirthDt 00000000\n" +
            "EmplJoe3      Smith     Developer ";

    String mixedTypesSplitRecordExample =
            "HEADERMy Title  26        EmplJoe1      Smith     Developer 07500010012009\n" +
            "CatSnowball  20200103\n" +
            "EmplJoe3      Smith     Developer ";

    String mixedTypesWrongSplitRecordExample =
            "HEADERMy Title  00        EmplJoe1      Smith     Developer 07500010012009\n" +
                    "CatSnowball  20200103\n" +
                    "EmplJoe3      Smith     Developer ";

    String mixedTypesCustomDelimiter =
            "EmplJoe1      Smith     Developer 07500010012009@" +
            "CatSnowball  20200103@" +
            "EmplJoe3      Smith     Developer ";

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineType() throws FixedLengthException {
        List<Row> parse = new FixedLength<Row>()
                .registerLineType(Employee.class)
                .parse(new ByteArrayInputStream(singleTypeExample.getBytes()));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with default charset and one line type")
    void testParseOneLineTypeUS_ACII() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(Employee.class)
                .usingCharset(StandardCharsets.US_ASCII)
                .parse(
                        new ByteArrayInputStream(singleTypeExample.getBytes(StandardCharsets.US_ASCII)));

        assertEquals(2, parse.size());
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type")
    void testParseMixedLineType() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesExample.getBytes()));

        assertEquals(4, parse.size());
        assertThat(parse.get(0), instanceOf(EmployeeMixed.class));
        assertThat(parse.get(1), instanceOf(CatMixed.class));
        assertThat(parse.get(2), instanceOf(CatMixed.class));
        assertThat(parse.get(3), instanceOf(EmployeeMixed.class));
        EmployeeMixed employeeMixed = (EmployeeMixed) parse.get(0);
        assertEquals("Joe1", employeeMixed.firstName);
        assertEquals("Smith", employeeMixed.lastName);
        CatMixed catMixed = (CatMixed) parse.get(1);
        assertEquals(LocalDate.of(2020, 1, 3), catMixed.birthDate);
        catMixed = (CatMixed) parse.get(2);
        assertNull(catMixed.birthDate);
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type with split record")
    void testParseMixedLineTypeSplit() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(HeaderSplit.class)
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesSplitRecordExample.getBytes()));

        assertEquals(4, parse.size());
        assertThat(parse.get(0), instanceOf(HeaderSplit.class));
        assertThat(parse.get(1), instanceOf(EmployeeMixed.class));
        assertThat(parse.get(2), instanceOf(CatMixed.class));
        assertThat(parse.get(3), instanceOf(EmployeeMixed.class));
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type with wrong split record")
    void testParseMixedLineTypeWrongSplit() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(HeaderSplit.class)
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .parse(new ByteArrayInputStream(mixedTypesWrongSplitRecordExample.getBytes()));

        assertEquals(3, parse.size());
        assertThat(parse.get(0), instanceOf(HeaderSplit.class));
        assertThat(parse.get(1), instanceOf(CatMixed.class));
        assertThat(parse.get(2), instanceOf(EmployeeMixed.class));
    }

    @Test
    @DisplayName("Parse as input stream with default charset and mixed line type and custom delimiter")
    void testParseMixedLineTypeCustomDelimiter() throws FixedLengthException {
        List<Object> parse = new FixedLength<>()
                .registerLineType(EmployeeMixed.class)
                .registerLineType(CatMixed.class)
                .usingLineDelimiter(Pattern.compile("@"))
                .parse(new ByteArrayInputStream(mixedTypesCustomDelimiter.getBytes()));

        assertEquals(3, parse.size());
    }
}
