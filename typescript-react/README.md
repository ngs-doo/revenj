# revenj

> Core library for DSL-generated React/TypeScript code

[![NPM](https://img.shields.io/npm/v/revenj.svg)](https://www.npmjs.com/package/revenj) [![JavaScript Style Guide](https://img.shields.io/badge/code_style-standard-brightgreen.svg)](https://standardjs.com)

## Install

```bash
npm install --save revenj
```

## Usage

```tsx
import React, { Component } from 'react'

import MyComponent from 'revenj'
import 'revenj/dist/index.css'

class Example extends Component {
  render() {
    return <MyComponent />
  }
}
```

## License

MIT © [the-overengineer](https://github.com/the-overengineer)
