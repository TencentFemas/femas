切换 tea2，表单部分请使用 duckComponents/teaform 下组件，用法如下：

```jsx
import { Form } from "@tencent/tea-component";
import FormField from "../duckComponents/teaform/Field";
import Input from "../duckComponents/teaform/Input";
<Form>
  <FormField label="备注" field={note}>
    <Input placeholder="选填，请输入备注信息" field={note} />
  </FormField>
</Form>;
```

与旧版对比，有以下差异：

- 各组件不再包含`form-unit`包装，不再处理校验信息，仅封装对应 tea2 组件的受控属性（value & onChange）
- 校验交由 `teaform/Field` 组件处理，它是 tea2 的 `Form.Item` 封装

另为了更灵活使用组件，现在 `FieldAPI` 支持 `map` 属性进行类型转换，这样就可以让数字 FieldAPI 简单用于`Switch`组件上

```jsx
<Switch
  field={numberField.map(
    (v) => !!v,
    (v) => (v ? 1 : 0)
  )}
/>
```
