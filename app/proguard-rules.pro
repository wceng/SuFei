# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Protobuf & DataStore
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep class * extends com.google.protobuf.MessageLite { *; }
-keep class com.google.protobuf.GeneratedMessageLite { *; }

# Keep DataStore serializers
-keep class * implements androidx.datastore.core.Serializer { *; }

# OpenCC4J - 保持所有类及成员（特别是反射调用的构造函数）
-keep class com.github.houbb.opencc4j.** { *; }
-dontwarn com.github.houbb.opencc4j.**

# Heaven - OpenCC4J 依赖的工具库，负责反射实例化逻辑
-keep class com.github.houbb.heaven.** { *; }
-dontwarn com.github.houbb.heaven.**

# nlp-common - 可能涉及的分词相关依赖
-keep class com.github.houbb.nlp.common.** { *; }
-dontwarn com.github.houbb.nlp.common.**
