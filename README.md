# i18n Locale Lens for JetBrains IDEs

Go to Declaration support for i18n keys stored in JSON locale files.

## Features

- Jump from `t('common.button.ok')` to the exact JSON property.
- Resolve namespace keys such as `auth:login.title`.
- Resolve simple template strings based on local string constants:

  ```ts
  const NOTIFICATION_SETTINGS_LOCAL_KEY = 'notification settings';
  t(`${NOTIFICATION_SETTINGS_LOCAL_KEY}.header text`);
  ```

- Configure locales, namespace mappings, key separators, and JSON path templates per project.

## Default Configuration

The default configuration supports locale files like:

```text
public/locales/ru/common.json
public/locales/ru/auth.json
public/locales/en/common.json
```

Default path template:

```text
public/locales/{locale}/{namespaceFile}.json
```

Default namespace file map:

```text
translation=common
common=common
```

## Build

```sh
gradle buildPlugin
```

The plugin archive will be created in `build/distributions`.

If Gradle cannot download the IntelliJ Platform plugin, build against a locally installed WebStorm SDK:

```sh
scripts/build-local-webstorm.sh
```

By default, the script uses `/Applications/WebStorm.app/Contents`. Override it when needed:

```sh
WEBSTORM_HOME="/path/to/WebStorm.app/Contents" scripts/build-local-webstorm.sh
```

## Publish

Create a JetBrains Marketplace token and publish with:

```sh
JETBRAINS_MARKETPLACE_TOKEN='<token>' gradle publishPlugin
```

