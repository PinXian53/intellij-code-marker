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

<b>Intellij Code Marker</b> is a developer productivity plugin.
It enables you to define custom markers for specific classes or methods by assigning meaningful icons—making key parts of your code instantly recognizable.

With intuitive visual cues embedded directly into the editor, Intellij Code Marker helps you identify important components at a glance, streamline navigation, and better understand your project’s structure.

## 🔧 Key Features

### 🎯 Custom Highlight Rules
Easily create rules by entering any class name, annotation or method name and selecting an icon from a built-in icon set.

Every field you fill in has to match, so a rule can be as broad as every call into an annotated class, or as narrow as a single method on a single class.
Class names and annotations may be written as a fully qualified name (`org.springframework.stereotype.Service`) or as a simple name (`Service`), and both are matched on super classes, interfaces and overridden methods as well.
<br>
![settings.png](pic/settings.png)

### 👁️ Visual Indicators in the Editor Gutter
Icons appear in the left gutter of the editor, right next to the corresponding code.
This makes it easy to visually scan and spot critical parts of your application without having to read every line.
<br>
![line-marker.png](pic/line-marker.png)

### 💬 Descriptive Tooltips
Hover over an icon in the gutter to display a tooltip with a custom description.
This gives you helpful context about the marked element’s role or purpose.
<br>
![line-marker-2.png](pic/line-marker-2.png)

## License & Attributions
See [ICON.md](./ICON.md) for icon sources and license details.
