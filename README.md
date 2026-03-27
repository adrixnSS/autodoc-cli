# 🚀 AutoDoc CLI - Generador Autónomo de Documentación

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Version](https://img.shields.io/badge/version-2.0.0--Universal-green.svg)
![GraalVM](https://img.shields.io/badge/GraalVM-Native%20Image-orange.svg)

**AutoDoc CLI** es un arquitecto de software en la sombra. Analiza tus cambios de código, poda la lógica interna para ahorrar tokens y genera documentación técnica profesional con diagramas Mermaid integrados en cuestión de segundos.

---

## ✨ Características Principales

- 🧠 **Podado Inteligente (AST/Regex):** Reduce el consumo de tokens en un ~80% al eliminar cuerpos de funciones e implementaciones, enviando solo las firmas a la IA.
- 🌍 **Soporte Universal:** Compatible con **Java, JavaScript, TypeScript y Python**. Incluye fallback para cualquier otro lenguaje.
- 💸 **Zero-Cost Mode:** Soporte nativo para **OpenClaw** y mocks locales para iterar sin gastar API Credits.
- 🛡️ **Seguridad de Grado Militar:** Generación automática de **SBOM (CycloneDX)** y compilación nativa con GraalVM.
- 🤖 **CI/CD Ready:** Integración perfecta con GitHub Actions para documentar Pull Requests automáticamente.

---

## 🚀 Instalación y Uso Rápido

### Requisitos
- Java 17+
- Git

### Uso Local (Recomendado para desarrollo)
```powershell
# Clonar y compilar
git clone https://github.com/adriansims/autodoc-cli.git
cd autodoc-cli
mvn clean package

# Ejecutar contra el último commit (Modo Gratis)
java -jar target/autodoc-cli-1.0-SNAPSHOT.jar --workspace . --diff HEAD~1 --local
```

### Uso con Gemini Cloud
```powershell
$env:GEMINI_API_KEY="tu_clave_api"
java -jar target/autodoc-cli-1.0-SNAPSHOT.jar --workspace . --diff origin/main
```

---

## 🛠️ Arquitectura Técnica

El CLI funciona en 4 pasos quirúrgicos:
1. **Git Analyzer:** Identifica archivos modificados mediante `git diff`.
2. **Universal Pruner:** Detecta el lenguaje y vacía el contenido de funciones/clases.
3. **LLM Orchestrator:** Envía el esqueleto del código a Gemini 1.5 Flash.
4. **Doc Writer:** Genera y formatea archivos Markdown en la carpeta `/docs`.

---

## 💰 Modelo de Negocio

AutoDoc CLI sigue un modelo **Freemium B2B**:
- **Open Source:** El CLI es gratuito para uso personal y local.
- **Enterprise SaaS:** Integración administrada como GitHub App para equipos corporativos (Subscripción mensual).

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Siéntete libre de abrir un Issue o enviar un Pull Request para añadir soporte a nuevos lenguajes.

---

Hecho con ❤️ por Adrian Sanchez Simon.
