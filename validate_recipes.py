#!/usr/bin/env python3
"""
Recipe Validator for Create Immersive TACZ
Validates all recipe JSON files in src/main/resources/data/createimmersivetacz/recipes
"""

import json
import os
from pathlib import Path
from typing import List, Dict, Any, Tuple

class RecipeValidator:
    def __init__(self, recipes_path: str = "src/main/resources/data/createimmersivetacz/recipes"):
        self.recipes_path = Path(recipes_path)
        self.errors: List[str] = []
        self.warnings: List[str] = []
        self.validated_files = 0

    def validate_all(self) -> bool:
        """Validate all recipe files. Returns True if all valid."""
        print("=== Validating Recipes ===")
        
        if not self.recipes_path.exists():
            self.add_error("Global", f"Recipes directory does not exist: {self.recipes_path}")
            return False
        
        recipe_files = list(self.recipes_path.rglob("*.json"))
        print(f"Found {len(recipe_files)} recipe files")
        
        for recipe_file in recipe_files:
            self.validate_recipe_file(recipe_file)
        
        self.print_results()
        return len(self.errors) == 0

    def validate_recipe_file(self, file_path: Path):
        """Validate a single recipe file."""
        relative_path = file_path.relative_to(self.recipes_path)
        print(f"Validating: {relative_path}")
        self.validated_files += 1
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                recipe = json.load(f)
            
            if not isinstance(recipe, dict):
                self.add_error(str(relative_path), "Root element is not a JSON object")
                return
            
            # Validate required 'type' field
            if 'type' not in recipe:
                self.add_error(str(relative_path), "Missing required field: 'type'")
                return
            
            recipe_type = recipe['type']
            
            # Validate based on recipe type
            validators = {
                'create:mechanical_crafting': self.validate_mechanical_crafting,
                'create:cutting': self.validate_cutting,
                'create:mixing': self.validate_mixing,
                'create:sequenced_assembly': self.validate_sequenced_assembly,
                'create:filling': self.validate_filling,
                'create:emptying': self.validate_emptying,
                'create:pressing': self.validate_pressing,
                'create:deploying': self.validate_deploying,
                'minecraft:crafting_shaped': self.validate_vanilla_crafting,
                'minecraft:crafting_shapeless': self.validate_vanilla_crafting,
            }
            
            validator = validators.get(recipe_type)
            if validator:
                validator(str(relative_path), recipe)
            else:
                self.add_warning(str(relative_path), f"Unknown recipe type: {recipe_type}")
        
        except json.JSONDecodeError as e:
            self.add_error(str(relative_path), f"Failed to parse JSON: {e}")
        except Exception as e:
            self.add_error(str(relative_path), f"Unexpected error: {e}")

    def validate_mechanical_crafting(self, path: str, recipe: Dict[str, Any]):
        """Validate mechanical crafting recipe."""
        if 'key' not in recipe:
            self.add_error(path, "Mechanical crafting missing 'key' field")
        elif not isinstance(recipe['key'], dict):
            self.add_error(path, "Field 'key' must be a JSON object")
        
        if 'pattern' not in recipe:
            self.add_error(path, "Mechanical crafting missing 'pattern' field")
        elif not isinstance(recipe['pattern'], list):
            self.add_error(path, "Field 'pattern' must be a JSON array")
        else:
            self.validate_pattern(path, recipe)
        
        if 'result' not in recipe:
            self.add_error(path, "Mechanical crafting missing 'result' field")
        else:
            self.validate_result(path, recipe['result'])
        
        if 'acceptMirrored' in recipe and not isinstance(recipe['acceptMirrored'], bool):
            self.add_error(path, "Field 'acceptMirrored' must be a boolean")

    def validate_cutting(self, path: str, recipe: Dict[str, Any]):
        """Validate cutting recipe."""
        if 'ingredients' not in recipe:
            self.add_error(path, "Cutting recipe missing 'ingredients' field")
        else:
            self.validate_ingredients(path, recipe['ingredients'])
        
        if 'results' not in recipe:
            self.add_error(path, "Cutting recipe missing 'results' field")
        elif not isinstance(recipe['results'], list):
            self.add_error(path, "Field 'results' must be a JSON array")
        else:
            self.validate_results(path, recipe['results'])
        
        if 'processingTime' in recipe and not isinstance(recipe['processingTime'], (int, float)):
            self.add_error(path, "Field 'processingTime' must be a number")

    def validate_mixing(self, path: str, recipe: Dict[str, Any]):
        """Validate mixing recipe."""
        if 'ingredients' not in recipe:
            self.add_error(path, "Mixing recipe missing 'ingredients' field")
        else:
            self.validate_ingredients(path, recipe['ingredients'])
        
        if 'results' not in recipe:
            self.add_error(path, "Mixing recipe missing 'results' field")
        elif not isinstance(recipe['results'], list):
            self.add_error(path, "Field 'results' must be a JSON array")
        else:
            self.validate_results(path, recipe['results'])
        
        if 'heatRequirement' in recipe:
            heat = recipe['heatRequirement']
            if heat not in ['none', 'heated', 'superheated']:
                self.add_error(path, f"Invalid heatRequirement: {heat}")

    def validate_sequenced_assembly(self, path: str, recipe: Dict[str, Any]):
        """Validate sequenced assembly recipe."""
        required_fields = ['ingredient', 'transitionalItem', 'sequence', 'results', 'loops']
        for field in required_fields:
            if field not in recipe:
                self.add_error(path, f"Sequenced assembly missing '{field}' field")
        
        if 'sequence' in recipe and not isinstance(recipe['sequence'], list):
            self.add_error(path, "Field 'sequence' must be a JSON array")

    def validate_filling(self, path: str, recipe: Dict[str, Any]):
        """Validate filling recipe."""
        if 'ingredients' not in recipe:
            self.add_error(path, "Filling recipe missing 'ingredients' field")
        if 'results' not in recipe:
            self.add_error(path, "Filling recipe missing 'results' field")

    def validate_emptying(self, path: str, recipe: Dict[str, Any]):
        """Validate emptying recipe."""
        if 'ingredients' not in recipe:
            self.add_error(path, "Emptying recipe missing 'ingredients' field")
        if 'results' not in recipe:
            self.add_error(path, "Emptying recipe missing 'results' field")

    def validate_pressing(self, path: str, recipe: Dict[str, Any]):
        """Validate pressing recipe."""
        if 'ingredients' not in recipe:
            self.add_error(path, "Pressing recipe missing 'ingredients' field")
        if 'results' not in recipe:
            self.add_error(path, "Pressing recipe missing 'results' field")

    def validate_deploying(self, path: str, recipe: Dict[str, Any]):
        """Validate deploying recipe."""
        if 'ingredients' not in recipe:
            self.add_error(path, "Deploying recipe missing 'ingredients' field")
        if 'results' not in recipe:
            self.add_error(path, "Deploying recipe missing 'results' field")

    def validate_vanilla_crafting(self, path: str, recipe: Dict[str, Any]):
        """Validate vanilla crafting recipe."""
        if 'result' not in recipe:
            self.add_error(path, "Vanilla crafting missing 'result' field")

    def validate_pattern(self, path: str, recipe: Dict[str, Any]):
        """Validate crafting pattern."""
        pattern = recipe['pattern']
        key = recipe.get('key', {})
        
        if not pattern:
            self.add_error(path, "Pattern array is empty")
            return
        
        # Collect all keys used in pattern
        used_keys = set()
        for row in pattern:
            if not isinstance(row, str):
                self.add_error(path, "Pattern row must be a string")
                continue
            for char in row:
                if char != ' ':
                    used_keys.add(char)
        
        # Check that all used keys are defined
        for char in used_keys:
            if char not in key:
                self.add_error(path, f"Pattern uses key '{char}' but it's not defined in 'key' object")
        
        # Warn about unused keys
        for defined_key in key.keys():
            if len(defined_key) == 1 and defined_key not in used_keys:
                self.add_warning(path, f"Key '{defined_key}' is defined but never used in pattern")

    def validate_ingredients(self, path: str, ingredients: Any):
        """Validate ingredients field."""
        if isinstance(ingredients, list):
            if not ingredients:
                self.add_warning(path, "Ingredients array is empty")
            for ingredient in ingredients:
                if not isinstance(ingredient, dict):
                    self.add_error(path, "Ingredient must be a JSON object")
                    continue
                self.validate_ingredient(path, ingredient)
        elif isinstance(ingredients, dict):
            self.validate_ingredient(path, ingredients)
        else:
            self.add_error(path, "Ingredients must be a JSON object or array")

    def validate_ingredient(self, path: str, ingredient: Dict[str, Any]):
        """Validate a single ingredient."""
        has_item = 'item' in ingredient
        has_tag = 'tag' in ingredient
        has_fluid = 'fluid' in ingredient
        
        if not (has_item or has_tag or has_fluid):
            self.add_error(path, "Ingredient must have 'item', 'tag', or 'fluid' field")
        
        type_count = sum([has_item, has_tag, has_fluid])
        if type_count > 1:
            self.add_warning(path, "Ingredient has multiple type fields (item/tag/fluid)")

    def validate_results(self, path: str, results: List[Any]):
        """Validate results array."""
        if not results:
            self.add_warning(path, "Results array is empty")
        for result in results:
            if not isinstance(result, dict):
                self.add_error(path, "Result must be a JSON object")
                continue
            self.validate_result(path, result)

    def validate_result(self, path: str, result: Any):
        """Validate a single result."""
        if not isinstance(result, dict):
            self.add_error(path, "Result must be a JSON object")
            return
        
        has_item = 'item' in result
        has_fluid = 'fluid' in result
        
        if not (has_item or has_fluid):
            self.add_error(path, "Result must have 'item' or 'fluid' field")
        
        # Validate count if present
        if 'count' in result:
            count = result['count']
            if isinstance(count, (int, float)) and count <= 0:
                self.add_error(path, f"Result count must be positive, got: {count}")
        
        # Validate amount if present (for fluids)
        if 'amount' in result:
            amount = result['amount']
            if isinstance(amount, (int, float)) and amount <= 0:
                self.add_error(path, f"Result amount must be positive, got: {amount}")

    def add_error(self, path: str, message: str):
        """Add an error message."""
        self.errors.append(f"[ERROR] {path}: {message}")

    def add_warning(self, path: str, message: str):
        """Add a warning message."""
        self.warnings.append(f"[WARNING] {path}: {message}")

    def print_results(self):
        """Print validation results."""
        if self.warnings:
            print("\n=== Warnings ===")
            for warning in self.warnings:
                print(warning)
        
        if self.errors:
            print("\n=== Errors ===")
            for error in self.errors:
                print(error)
            print(f"\n❌ Found {len(self.errors)} error(s) in recipes")
        else:
            print("\n=== Validation Complete ===")
            print(f"✓ All {self.validated_files} recipes are valid!")


def main():
    """Main entry point."""
    validator = RecipeValidator()
    success = validator.validate_all()
    exit(0 if success else 1)


if __name__ == "__main__":
    main()
