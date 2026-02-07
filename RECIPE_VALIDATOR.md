# Recipe Validator

This project includes validators to verify the integrity and structure of all recipes in `src/main/resources/data/createimmersivetacz/recipes`.

## Available Validators

### 1. Python Validator (Recommended)

**Location:** `validate_recipes.py`

**Advantages:**
- Fast and easy to run
- No Gradle configuration required
- Works with any Python 3.6+ version

**Usage:**
```bash
python validate_recipes.py
```

**Requirements:**
- Python 3.6 or higher

### 2. Java Validator (JUnit Test)

**Location:** `src/test/java/net/myr/RecipeTest.java`

**Advantages:**
- Integrated with the project's test system
- Uses JUnit 5
- Runs as part of the project's tests

**Usage:**
```bash
./gradlew test --tests RecipeTest
```

**Requirements:**
- Java 17
- Gradle properly configured

> **Note:** If you have issues with the Java version, use the Python validator.

## Validated Recipe Types

The validators support the following recipe types:

### Create Mod
- `create:mechanical_crafting` - Mechanical crafting with pattern and keys
- `create:cutting` - Cutting recipes
- `create:mixing` - Mixing recipes
- `create:sequenced_assembly` - Sequenced assembly
- `create:filling` - Filling items with fluids
- `create:emptying` - Emptying fluids from items
- `create:pressing` - Pressing items
- `create:deploying` - Deploying items

### Vanilla Minecraft
- `minecraft:crafting_shaped` - Shaped crafting
- `minecraft:crafting_shapeless` - Shapeless crafting

## Validations Performed

### General Validations
- ✓ Valid JSON syntax
- ✓ Required `type` field present
- ✓ Required fields for each recipe type
- ✓ Correct data types (objects, arrays, numbers, etc.)

### Specific Validations

#### Mechanical Crafting
- ✓ `key` field present and is an object
- ✓ `pattern` field present and is an array
- ✓ `result` field present and valid
- ✓ All keys used in the pattern are defined
- ⚠️ Warning if there are defined but unused keys

#### Cutting, Mixing, Pressing, Deploying
- ✓ `ingredients` field present and valid
- ✓ `results` field present and is an array
- ✓ `processingTime` is a number (if present)
- ✓ `heatRequirement` is valid: "none", "heated", or "superheated" (for mixing)

#### Ingredients
- ✓ Each ingredient has `item`, `tag` or `fluid`
- ⚠️ Warning if an ingredient has multiple types

#### Results
- ✓ Each result has `item` or `fluid`
- ✓ `count` and `amount` values are positive

## Output Examples

### Successful Validation
```
=== Validating Recipes ===
Found 57 recipe files
Validating: firing_mechanism.json
Validating: gunbarrel.json
...
=== Validation Complete ===
✓ All 57 recipes are valid!
```

### With Errors
```
=== Validating Recipes ===
Found 57 recipe files
Validating: example.json

=== Errors ===
[ERROR] example.json: Missing required field: 'type'
[ERROR] example.json: Mechanical crafting missing 'key' field

❌ Found 2 error(s) in recipes
```

### With Warnings
```
=== Warnings ===
[WARNING] example.json: Key 'X' is defined but never used in pattern
[WARNING] example.json: Ingredients array is empty
```

## Continuous Integration

You can add the validator to your workflow:

### Pre-commit hook
```bash
#!/bin/bash
python validate_recipes.py
if [ $? -ne 0 ]; then
    echo "Recipe validation failed!"
    exit 1
fi
```

### GitHub Actions
```yaml
- name: Validate recipes
  run: python validate_recipes.py
```

## Troubleshooting

### Error: "Recipes directory does not exist"
Make sure to run the validator from the project root where `src/main/resources/data/createimmersivetacz/recipes` is located.

### Error: "Failed to parse JSON"
There is a syntax error in the JSON file. Check:
- Commas, quotes, and braces correctly placed
- UTF-8 encoding
- No invalid special characters

### Java Validator: "Unsupported class file major version"
This error occurs when there is a mismatch between the compiled code's Java version and the Gradle version. Use the Python validator as an alternative.

## Contributing

To add support for new recipe types:

1. **Python**: Add a `validate_<type>` method in the `RecipeValidator` class
2. **Java**: Add a `validate<Type>` method in the `RecipeTest` class
3. Register the validator in the corresponding dictionary/switch
4. Update this documentation

## License

This code is available under the same license as the main project.
