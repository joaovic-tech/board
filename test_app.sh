#!/bin/bash
# Script para testar a aplicação automaticamente

echo "Testando aplicação Board..."

# Simular entrada: criar board, depois sair
echo -e "1\nBoard Teste\n0" | timeout 10 ./gradlew run 2>&1 | head -50
