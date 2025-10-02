package pl.kurs.test3r.contracts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import pl.kurs.test3r.commands.CreateEmployeeCommand;
import pl.kurs.test3r.commands.CreatePersonCommand;
import pl.kurs.test3r.commands.CreateRetireeCommand;
import pl.kurs.test3r.commands.CreateStudentCommand;
import pl.kurs.test3r.commands.UpdateEmployeeCommand;
import pl.kurs.test3r.commands.UpdatePersonCommand;
import pl.kurs.test3r.commands.UpdateRetireeCommand;
import pl.kurs.test3r.commands.UpdateStudentCommand;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonCommandDeserializationContractTest {

    @ParameterizedTest
    @MethodSource("createCommandPayloads")
    void shouldDeserializeCreateCommands(String payload, Class<? extends CreatePersonCommand> expectedType) throws Exception {
        ObjectMapper mapper = buildMapper();
        CreatePersonCommand command = mapper.readValue(payload, CreatePersonCommand.class);

        assertThat(command).isInstanceOf(expectedType);
        assertThat(command.getType()).isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("updateCommandPayloads")
    void shouldDeserializeUpdateCommands(String payload, Class<? extends UpdatePersonCommand> expectedType) throws Exception {
        ObjectMapper mapper = buildMapper();
        UpdatePersonCommand command = mapper.readValue(payload, UpdatePersonCommand.class);

        assertThat(command).isInstanceOf(expectedType);
        assertThat(command.getType()).isNotBlank();
    }

    private static Stream<Arguments> createCommandPayloads() throws JsonProcessingException {
        ObjectMapper mapper = buildMapper();

        ObjectNode student = baseCreateNode(mapper, "STUDENT");
        student.put("university", "University");
        student.put("studyYear", 2);
        student.put("fieldOfStudy", "IT");
        student.put("scholarship", 1500.0);

        ObjectNode employee = baseCreateNode(mapper, "EMPLOYEE");
        employee.put("startDate", "2020-01-01");
        employee.put("currentPosition", "Developer");
        employee.put("currentSalary", 8000.0);

        ObjectNode retiree = baseCreateNode(mapper, "RETIREE");
        retiree.put("pensionAmount", 3200.0);
        retiree.put("yearsWorked", 35);

        return Stream.of(
                Arguments.of(write(mapper, student), CreateStudentCommand.class),
                Arguments.of(write(mapper, employee), CreateEmployeeCommand.class),
                Arguments.of(write(mapper, retiree), CreateRetireeCommand.class)
        );
    }

    private static Stream<Arguments> updateCommandPayloads() throws JsonProcessingException {
        ObjectMapper mapper = buildMapper();

        ObjectNode student = baseUpdateNode(mapper, "STUDENT");
        student.put("university", "University");
        student.put("studyYear", 3);
        student.put("fieldOfStudy", "Math");
        student.put("scholarship", 1700.0);

        ObjectNode employee = baseUpdateNode(mapper, "EMPLOYEE");
        employee.put("startDate", "2019-06-15");
        employee.put("currentPosition", "Lead Developer");
        employee.put("currentSalary", 9000.0);

        ObjectNode retiree = baseUpdateNode(mapper, "RETIREE");
        retiree.put("pensionAmount", 3300.0);
        retiree.put("yearsWorked", 36);

        return Stream.of(
                Arguments.of(write(mapper, student), UpdateStudentCommand.class),
                Arguments.of(write(mapper, employee), UpdateEmployeeCommand.class),
                Arguments.of(write(mapper, retiree), UpdateRetireeCommand.class)
        );
    }

    private static ObjectNode baseCreateNode(ObjectMapper mapper, String type) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", type);
        node.put("firstName", "John");
        node.put("lastName", "Doe");
        node.put("pesel", "12345678901");
        node.put("height", 180.0);
        node.put("weight", 75.0);
        node.put("email", "john.doe@example.com");
        return node;
    }

    private static ObjectNode baseUpdateNode(ObjectMapper mapper, String type) {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", type);
        node.put("id", 1L);
        node.put("version", 0L);
        node.put("lastName", "Doe");
        node.put("pesel", "12345678901");
        node.put("height", 180.0);
        node.put("weight", 75.0);
        node.put("email", "john.doe@example.com");
        return node;
    }

    private static String write(ObjectMapper mapper, ObjectNode node) throws JsonProcessingException {
        return mapper.writeValueAsString(node);
    }

    private static ObjectMapper buildMapper() {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().build();
        mapper.findAndRegisterModules();
        return mapper;
    }
}