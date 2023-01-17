# react-native-stl-api

Common API that STL uses.

## Installation

```sh
yarn add @stl1/react-native-stl-api
```
or
```sh
npm i @stl1/react-native-stl-api
```

## Usage

```js
import STL from "react-native-stl-api";

// ...

const keyHashes = await STL.common.getKeyHashes();

const nearbyDevice = await STL.register.getNearbyDevice();
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
