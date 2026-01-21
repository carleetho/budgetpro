#!/bin/bash
# Script de prueba para verificar que el CLI funciona correctamente
# Ejecutar después de compilar: mvn clean package

JAR_FILE="target/domain-validator-1.0.0-SNAPSHOT.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: JAR no encontrado. Ejecuta primero: mvn clean package"
    exit 1
fi

echo "🧪 Probando Domain Validator CLI..."
echo ""

echo "1️⃣  Probando comando de ayuda general:"
java -jar "$JAR_FILE" --help
echo ""

echo "2️⃣  Probando subcomando validate --help:"
java -jar "$JAR_FILE" validate --help
echo ""

echo "3️⃣  Probando subcomando generate-roadmap --help:"
java -jar "$JAR_FILE" generate-roadmap --help
echo ""

echo "4️⃣  Probando subcomando check-module --help:"
java -jar "$JAR_FILE" check-module --help
echo ""

echo "5️⃣  Probando ejecución de validate (debe retornar exit code 0):"
java -jar "$JAR_FILE" validate --repo-path ../../backend
EXIT_CODE=$?
echo "Exit code: $EXIT_CODE"
echo ""

if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ Todas las pruebas pasaron correctamente"
    exit 0
else
    echo "❌ Algunas pruebas fallaron"
    exit 1
fi
