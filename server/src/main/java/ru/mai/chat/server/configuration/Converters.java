package ru.mai.chat.server.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import ru.mai.chat.common.AlgorithmEnum;
import ru.mai.chat.common.EncryptionModeEnum;
import ru.mai.chat.common.PaddingModeEnum;

import java.sql.JDBCType;

public final class Converters {

    // region AlgorithmEnum
    @WritingConverter
    enum AlgorithmEnumToJdbcValue implements Converter<AlgorithmEnum, JdbcValue> {
        INSTANCE;

        @Override
        public JdbcValue convert(AlgorithmEnum source) {
            return JdbcValue.of(source.getValueDescriptor().getName(), JDBCType.OTHER);
        }

    }

    @ReadingConverter
    enum StringToAlgorithmEnum implements Converter<String, AlgorithmEnum> {
        INSTANCE;

        @Override
        public AlgorithmEnum convert(String source) {
            return AlgorithmEnum.valueOf(source);
        }

    }
    // endregion

    // region EncryptionModeEnum
    @WritingConverter
    enum EncryptionModeEnumToJdbcValue implements Converter<EncryptionModeEnum, JdbcValue> {
        INSTANCE;

        @Override
        public JdbcValue convert(EncryptionModeEnum source) {
            return JdbcValue.of(source.getValueDescriptor().getName(), JDBCType.OTHER);
        }

    }

    @ReadingConverter
    enum StringToEncryptionModeEnum implements Converter<String, EncryptionModeEnum> {
        INSTANCE;

        @Override
        public EncryptionModeEnum convert(String source) {
            return EncryptionModeEnum.valueOf(source);
        }

    }
    // endregion

    // region PaddingModeEnum
    @WritingConverter
    enum PaddingModeEnumToJdbcValue implements Converter<PaddingModeEnum, JdbcValue> {
        INSTANCE;

        @Override
        public JdbcValue convert(PaddingModeEnum source) {
            return JdbcValue.of(source.getValueDescriptor().getName(), JDBCType.OTHER);
        }

    }

    @ReadingConverter
    enum StringToPaddingModeEnum implements Converter<String, PaddingModeEnum> {
        INSTANCE;

        @Override
        public PaddingModeEnum convert(String source) {
            return PaddingModeEnum.valueOf(source);
        }

    }
    // endregion

}
