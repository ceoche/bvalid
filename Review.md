## Framework

- add Function<T, ?>... actualValueSuppliers to the signature of the 2 existing addRule methods in ValidatorBuilder


## BValid

- Same strategy for BValidatorBuilder, let the signature with var args to keep only 2 methods

### BValidator
- Add dedicated record to store actual value suppliers and their name
- Changed signature of all methods to take var args of ActualValueSuppliers
- Add a Map<name, actualValue> in AssertionReport
- Add Location in AssertionReport
- Remove location computation from toString, instead use actual values and location attributes from AssertionReport