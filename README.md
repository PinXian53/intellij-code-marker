<div align="center">
    <a href="https://plugins.jetbrains.com/plugin/27955-code-marker">
        <img src="./src/main/resources/META-INF/pluginIcon.svg" width="280" height="280" alt="logo"/>
    </a>
</div>

<h1 align="center">Intellij Code Marker</h1>

<p align="center">
<a href="https://plugins.jetbrains.com/plugin/27955-code-marker"><img src="https://img.shields.io/jetbrains/plugin/r/stars/27955?style=flat-square"></a>
<a href="https://plugins.jetbrains.com/plugin/27955-code-marker"><img src="https://img.shields.io/jetbrains/plugin/d/27955-code-marker.svg?style=flat-square"></a>
<a href="https://plugins.jetbrains.com/plugin/27955-code-marker"><img src="https://img.shields.io/jetbrains/plugin/v/27955-code-marker.svg?style=flat-square"></a>
</p>

<br>

> Jetbrains Marketplace: https://plugins.jetbrains.com/plugin/27955-code-marker

<b>Code Marker</b> puts an icon in the editor gutter next to Java method calls that match rules you define,
so the calls that matter in your project, such as database access, remote calls or transactional services,
stand out while you read the code.

## Features

### Rules by Class, Annotation and Method
Match calls by the class that declares the method, by an annotation on the method or its class, by the method name, or any combination of them.
Every field you fill in has to match, so a rule can be as broad as every call into an annotated class, or as narrow as a single method on a single class.

- **Inheritance aware**: super classes, interfaces and overridden methods are checked as well, so a rule on an interface also marks calls through its implementations.
- **Simple or fully qualified names**: write `Service` or `org.springframework.stereotype.Service`, with or without the leading `@`.
- **Built-in icon set**: pick an icon for each rule from the bundled icons.

![settings.png](pic/settings.png)

### Ordered Rules, Import and Export
Rules are checked from top to bottom and the first match wins.
Reorder them by dragging a row or with the arrow buttons in the toolbar.
The toolbar also saves the rule list to an XML file and loads it back, so you can share your rules with your team.

### Gutter Icons
Matching method calls get their rule's icon in the left gutter, so you can spot them without reading every line.

![line-marker.png](pic/line-marker.png)

### Documentation on Hover
Hovering a gutter icon shows the documentation of the called method.

![line-marker-2.png](pic/line-marker-2.png)

## Getting Started
1. Open **Settings | Tools | Code Marker**.
2. Add a rule and fill in a class name, an annotation, or both. The method name is optional; leaving it empty matches every method.
3. Choose an icon and apply. Open editors are updated right away.

## License & Attributions
See [ICON.md](./ICON.md) for icon sources and license details.
