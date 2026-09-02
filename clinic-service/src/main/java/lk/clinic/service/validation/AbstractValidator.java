package lk.clinic.service.validation;

public abstract class AbstractValidator<T> {
    public final ValidationResult validate(T target) {
        ValidationResult result = new ValidationResult();
        doValidate(target, result);
        return result;
    }

    protected abstract void doValidate(T target, ValidationResult result);
}
