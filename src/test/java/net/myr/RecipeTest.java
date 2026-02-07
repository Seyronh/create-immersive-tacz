import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeTest {

    private static final String RECIPES_PATH = "src/main/resources/data/createimmersivetacz/recipes";
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    @Test
    public void testAllRecipesAreValid() {
        System.out.println("=== Validating Recipes ===");
        
        File recipesDir = new File(RECIPES_PATH);
        assertTrue(recipesDir.exists(), "Recipes directory does not exist: " + RECIPES_PATH);
        assertTrue(recipesDir.isDirectory(), "Recipes path is not a directory: " + RECIPES_PATH);

        List<File> recipeFiles = findAllJsonFiles(recipesDir);
        System.out.println("Found " + recipeFiles.size() + " recipe files");

        for (File recipeFile : recipeFiles) {
            validateRecipeFile(recipeFile);
        }

        // Print all warnings
        if (!warnings.isEmpty()) {
            System.out.println("\n=== Warnings ===");
            warnings.forEach(System.out::println);
        }

        // Print all errors
        if (!errors.isEmpty()) {
            System.out.println("\n=== Errors ===");
            errors.forEach(System.err::println);
            fail("Found " + errors.size() + " error(s) in recipes. Check console output.");
        }

        System.out.println("\n=== Validation Complete ===");
        System.out.println("✓ All " + recipeFiles.size() + " recipes are valid!");
    }

    private List<File> findAllJsonFiles(File directory) {
        List<File> jsonFiles = new ArrayList<>();
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    jsonFiles.addAll(findAllJsonFiles(file));
                } else if (file.getName().endsWith(".json")) {
                    jsonFiles.add(file);
                }
            }
        }
        
        return jsonFiles;
    }

    private void validateRecipeFile(File file) {
        String relativePath = file.getPath().replace(RECIPES_PATH, "").replace("\\", "/");
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        
        System.out.println("Validating: " + relativePath);

        try (FileReader reader = new FileReader(file)) {
            JsonElement element = JsonParser.parseReader(reader);
            
            if (!element.isJsonObject()) {
                addError(relativePath, "Root element is not a JSON object");
                return;
            }

            JsonObject recipe = element.getAsJsonObject();
            
            // Validate required 'type' field
            if (!recipe.has("type")) {
                addError(relativePath, "Missing required field: 'type'");
                return;
            }

            String type = recipe.get("type").getAsString();
            
            // Validate based on recipe type
            switch (type) {
                case "create:mechanical_crafting":
                    validateMechanicalCrafting(relativePath, recipe);
                    break;
                case "create:cutting":
                    validateCutting(relativePath, recipe);
                    break;
                case "create:mixing":
                    validateMixing(relativePath, recipe);
                    break;
                case "create:sequenced_assembly":
                    validateSequencedAssembly(relativePath, recipe);
                    break;
                case "create:filling":
                    validateFilling(relativePath, recipe);
                    break;
                case "create:emptying":
                    validateEmptying(relativePath, recipe);
                    break;
                case "create:pressing":
                    validatePressing(relativePath, recipe);
                    break;
                case "create:deploying":
                    validateDeploying(relativePath, recipe);
                    break;
                case "minecraft:crafting_shaped":
                case "minecraft:crafting_shapeless":
                    validateVanillaCrafting(relativePath, recipe);
                    break;
                default:
                    addWarning(relativePath, "Unknown recipe type: " + type);
                    break;
            }

        } catch (Exception e) {
            addError(relativePath, "Failed to parse JSON: " + e.getMessage());
        }
    }

    private void validateMechanicalCrafting(String path, JsonObject recipe) {
        // Validate 'key' field
        if (!recipe.has("key")) {
            addError(path, "Mechanical crafting missing 'key' field");
        } else if (!recipe.get("key").isJsonObject()) {
            addError(path, "Field 'key' must be a JSON object");
        }

        // Validate 'pattern' field
        if (!recipe.has("pattern")) {
            addError(path, "Mechanical crafting missing 'pattern' field");
        } else if (!recipe.get("pattern").isJsonArray()) {
            addError(path, "Field 'pattern' must be a JSON array");
        } else {
            validatePattern(path, recipe);
        }

        // Validate 'result' field
        if (!recipe.has("result")) {
            addError(path, "Mechanical crafting missing 'result' field");
        } else {
            validateResult(path, recipe.get("result"));
        }

        // Validate acceptMirrored (optional)
        if (recipe.has("acceptMirrored") && !recipe.get("acceptMirrored").isJsonPrimitive()) {
            addError(path, "Field 'acceptMirrored' must be a boolean");
        }
    }

    private void validateCutting(String path, JsonObject recipe) {
        // Validate 'ingredients' field
        if (!recipe.has("ingredients")) {
            addError(path, "Cutting recipe missing 'ingredients' field");
        } else {
            validateIngredients(path, recipe.get("ingredients"));
        }

        // Validate 'results' field
        if (!recipe.has("results")) {
            addError(path, "Cutting recipe missing 'results' field");
        } else if (!recipe.get("results").isJsonArray()) {
            addError(path, "Field 'results' must be a JSON array");
        } else {
            validateResults(path, recipe.get("results").getAsJsonArray());
        }

        // Validate 'processingTime' (optional but common)
        if (recipe.has("processingTime")) {
            if (!recipe.get("processingTime").isJsonPrimitive()) {
                addError(path, "Field 'processingTime' must be a number");
            }
        }
    }

    private void validateMixing(String path, JsonObject recipe) {
        // Validate 'ingredients' field
        if (!recipe.has("ingredients")) {
            addError(path, "Mixing recipe missing 'ingredients' field");
        } else {
            validateIngredients(path, recipe.get("ingredients"));
        }

        // Validate 'results' field
        if (!recipe.has("results")) {
            addError(path, "Mixing recipe missing 'results' field");
        } else if (!recipe.get("results").isJsonArray()) {
            addError(path, "Field 'results' must be a JSON array");
        } else {
            validateResults(path, recipe.get("results").getAsJsonArray());
        }

        // Validate heatRequirement (optional)
        if (recipe.has("heatRequirement")) {
            String heat = recipe.get("heatRequirement").getAsString();
            if (!heat.equals("none") && !heat.equals("heated") && !heat.equals("superheated")) {
                addError(path, "Invalid heatRequirement: " + heat);
            }
        }
    }

    private void validateSequencedAssembly(String path, JsonObject recipe) {
        if (!recipe.has("ingredient")) {
            addError(path, "Sequenced assembly missing 'ingredient' field");
        }
        if (!recipe.has("transitionalItem")) {
            addError(path, "Sequenced assembly missing 'transitionalItem' field");
        }
        if (!recipe.has("sequence")) {
            addError(path, "Sequenced assembly missing 'sequence' field");
        } else if (!recipe.get("sequence").isJsonArray()) {
            addError(path, "Field 'sequence' must be a JSON array");
        }
        if (!recipe.has("results")) {
            addError(path, "Sequenced assembly missing 'results' field");
        }
        if (!recipe.has("loops")) {
            addError(path, "Sequenced assembly missing 'loops' field");
        }
    }

    private void validateFilling(String path, JsonObject recipe) {
        if (!recipe.has("ingredients")) {
            addError(path, "Filling recipe missing 'ingredients' field");
        }
        if (!recipe.has("results")) {
            addError(path, "Filling recipe missing 'results' field");
        }
    }

    private void validateEmptying(String path, JsonObject recipe) {
        if (!recipe.has("ingredients")) {
            addError(path, "Emptying recipe missing 'ingredients' field");
        }
        if (!recipe.has("results")) {
            addError(path, "Emptying recipe missing 'results' field");
        }
    }

    private void validatePressing(String path, JsonObject recipe) {
        if (!recipe.has("ingredients")) {
            addError(path, "Pressing recipe missing 'ingredients' field");
        }
        if (!recipe.has("results")) {
            addError(path, "Pressing recipe missing 'results' field");
        }
    }

    private void validateDeploying(String path, JsonObject recipe) {
        if (!recipe.has("ingredients")) {
            addError(path, "Deploying recipe missing 'ingredients' field");
        }
        if (!recipe.has("results")) {
            addError(path, "Deploying recipe missing 'results' field");
        }
    }

    private void validateVanillaCrafting(String path, JsonObject recipe) {
        if (!recipe.has("result")) {
            addError(path, "Vanilla crafting missing 'result' field");
        }
        // Additional validation could be added for shaped/shapeless specific fields
    }

    private void validatePattern(String path, JsonObject recipe) {
        JsonArray pattern = recipe.getAsJsonArray("pattern");
        JsonObject key = recipe.getAsJsonObject("key");

        if (pattern.size() == 0) {
            addError(path, "Pattern array is empty");
            return;
        }

        // Collect all keys used in pattern
        List<Character> usedKeys = new ArrayList<>();
        for (JsonElement row : pattern) {
            if (!row.isJsonPrimitive()) {
                addError(path, "Pattern row must be a string");
                continue;
            }
            String rowStr = row.getAsString();
            for (char c : rowStr.toCharArray()) {
                if (c != ' ' && !usedKeys.contains(c)) {
                    usedKeys.add(c);
                }
            }
        }

        // Check that all used keys are defined
        for (char c : usedKeys) {
            String keyStr = String.valueOf(c);
            if (!key.has(keyStr)) {
                addError(path, "Pattern uses key '" + c + "' but it's not defined in 'key' object");
            }
        }

        // Warn about unused keys
        for (String definedKey : key.keySet()) {
            if (definedKey.length() == 1) {
                char keyChar = definedKey.charAt(0);
                if (!usedKeys.contains(keyChar)) {
                    addWarning(path, "Key '" + keyChar + "' is defined but never used in pattern");
                }
            }
        }
    }

    private void validateIngredients(String path, JsonElement ingredientsElement) {
        if (ingredientsElement.isJsonArray()) {
            JsonArray ingredients = ingredientsElement.getAsJsonArray();
            if (ingredients.size() == 0) {
                addWarning(path, "Ingredients array is empty");
            }
            for (JsonElement ingredient : ingredients) {
                if (!ingredient.isJsonObject()) {
                    addError(path, "Ingredient must be a JSON object");
                    continue;
                }
                validateIngredient(path, ingredient.getAsJsonObject());
            }
        } else if (ingredientsElement.isJsonObject()) {
            validateIngredient(path, ingredientsElement.getAsJsonObject());
        } else {
            addError(path, "Ingredients must be a JSON object or array");
        }
    }

    private void validateIngredient(String path, JsonObject ingredient) {
        boolean hasItem = ingredient.has("item");
        boolean hasTag = ingredient.has("tag");
        boolean hasFluid = ingredient.has("fluid");

        if (!hasItem && !hasTag && !hasFluid) {
            addError(path, "Ingredient must have 'item', 'tag', or 'fluid' field");
        }
        if ((hasItem && hasTag) || (hasItem && hasFluid) || (hasTag && hasFluid)) {
            addWarning(path, "Ingredient has multiple type fields (item/tag/fluid)");
        }
    }

    private void validateResults(String path, JsonArray results) {
        if (results.size() == 0) {
            addWarning(path, "Results array is empty");
        }
        for (JsonElement result : results) {
            if (!result.isJsonObject()) {
                addError(path, "Result must be a JSON object");
                continue;
            }
            validateResult(path, result);
        }
    }

    private void validateResult(String path, JsonElement resultElement) {
        if (!resultElement.isJsonObject()) {
            addError(path, "Result must be a JSON object");
            return;
        }

        JsonObject result = resultElement.getAsJsonObject();
        boolean hasItem = result.has("item");
        boolean hasFluid = result.has("fluid");

        if (!hasItem && !hasFluid) {
            addError(path, "Result must have 'item' or 'fluid' field");
        }

        // Validate count if present
        if (result.has("count")) {
            JsonElement count = result.get("count");
            if (count.isJsonPrimitive() && count.getAsJsonPrimitive().isNumber()) {
                int countValue = count.getAsInt();
                if (countValue <= 0) {
                    addError(path, "Result count must be positive, got: " + countValue);
                }
            }
        }

        // Validate amount if present (for fluids)
        if (result.has("amount")) {
            JsonElement amount = result.get("amount");
            if (amount.isJsonPrimitive() && amount.getAsJsonPrimitive().isNumber()) {
                int amountValue = amount.getAsInt();
                if (amountValue <= 0) {
                    addError(path, "Result amount must be positive, got: " + amountValue);
                }
            }
        }
    }

    private void addError(String path, String message) {
        String error = "[ERROR] " + path + ": " + message;
        errors.add(error);
    }

    private void addWarning(String path, String message) {
        String warning = "[WARNING] " + path + ": " + message;
        warnings.add(warning);
    }
}
