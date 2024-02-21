package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
class CreditResourceTest {
    @Autowired
    private lateinit var customerRepository: CustomerRepository
    @Autowired
    private lateinit var creditRepository: CreditRepository
    @Autowired
    private lateinit var mockMvc: MockMvc
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    companion object {
        const val URL: String = "/api/credits"
        const val URL0: String = "/api/customers"
    }

    @BeforeEach
    fun setup() = creditRepository.deleteAll()


    @AfterEach
    fun tearDown() = creditRepository.deleteAll()

    @Test
    fun `should create credit and return 201 status`(){
        //given
        val customerDto: CustomerDto = builderCustomerDto()
        val valueAsString0 = objectMapper.writeValueAsString(customerDto)

        mockMvc.perform(
            MockMvcRequestBuilders.post(CreditResourceTest.URL0)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString0)
        )
        val creditDto: CreditDto = builderCreditDto()
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(valueAsString)
        )
            .andExpect{MockMvcResultMatchers.status().isCreated}
            //.andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value("500.0"))
            //.andExpect(MockMvcResultMatchers.jsonPath("$.data.dayFirstOfInstallment").value("2024-04-22"))
            //.andExpect(MockMvcResultMatchers.jsonPath("$.data.numberOfInstallments").value("4"))
            //.andExpect(MockMvcResultMatchers.jsonPath("$.data.customerId").value("4"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should not create credit given invalid customerId and return 400 status`(){
        //given
        val invalidId: Long = 72
        val creditDto: CreditDto = builderCreditDto(customerId = invalidId)
        val valueAsString: String = objectMapper.writeValueAsString(creditDto)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(valueAsString)
        )
            .andExpect{MockMvcResultMatchers.status().isBadRequest}
    }
    @Test
    fun `should find all by customerId and return 200 status`(){
        //given
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())

        val credit1: Credit = creditRepository.save(builderCreditDto().toEntity())

        val credit2: Credit = creditRepository.save(builderCreditDto().toEntity())

        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("${URL}?customerId=${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }
    @Test
    fun`should find by credit code and return 200 status`(){
        //given
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${credit.creditCode}?customerId=${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
    }
    @Test
    fun`should not find by invalid credit code and return 400 status`(){
        //given
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        val invalidCreditCode: UUID = UUID.randomUUID()
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${invalidCreditCode}?customerId=${customer.id}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }
    @Test
    fun`should not find credit by different customerId and return 400 status`(){
        //given
        val invalidId: Long = 64
        val customer: Customer = customerRepository.save(builderCustomerDto().toEntity())
        val credit: Credit = creditRepository.save(builderCreditDto().toEntity())
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("${URL}/${credit.creditCode}?customerId=${invalidId}")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andDo(MockMvcResultHandlers.print())
    }
    private fun builderCreditDto(
        creditValue: BigDecimal = BigDecimal.valueOf(500.0),
        dayFirstInstallment: LocalDate = LocalDate.of(2024, Month.APRIL, 22),
        numberOfInstallments: Int = 4,
        customerId: Long = 1
    ) = CreditDto(
        creditValue = creditValue,
        dayFirstOfInstallment = dayFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )
    private fun builderCustomerDto(
        firstName: String = "nome",
        lastName: String = "sobrenome",
        cpf: String = "78240212034",
        email: String ="joe@gmail.com",
        password: String = "senhaconfiavel",
        zipCode: String = "986412",
        street: String = "rua tranquila",
        income: BigDecimal = BigDecimal.valueOf(1000.0),

        ) = CustomerDto(
        firstName = firstName,
        lastName = lastName,
        cpf = cpf,
        email = email,
        password = password,

        zipCode = zipCode,
        street = street
        ,
        income = income,
    )
}