package ru.mai.chat.server.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import java.util.ArrayList;

@Configuration
public class DataJdbcConfiguration {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        var converters = new ArrayList<>();

        converters.add(Converters.AlgorithmEnumToJdbcValue.INSTANCE);
        converters.add(Converters.StringToAlgorithmEnum.INSTANCE);
        converters.add(Converters.EncryptionModeEnumToJdbcValue.INSTANCE);
        converters.add(Converters.StringToEncryptionModeEnum.INSTANCE);
        converters.add(Converters.PaddingModeEnumToJdbcValue.INSTANCE);
        converters.add(Converters.StringToPaddingModeEnum.INSTANCE);

        return new JdbcCustomConversions(converters);
    }

}
