package jdk8;

@FunctionalInterface
public interface IConverter<F, T> {

    T convert(F from);

}
