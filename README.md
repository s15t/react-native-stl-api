# react-native-stl-api

Common API that STL uses.

## Installation

```sh
npm install --save s15t/react-native-stl-api#main
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
