#!/usr/bin/env sh
# Ejecuta la consola del ATM: ./atm.sh <comando> [argumentos]
# Requiere haber generado el JAR con "mvn clean package".
exec java -jar "$(dirname "$0")/target/atm-console.jar" "$@"
