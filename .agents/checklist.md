# Pre-Commit Checklist

- [ ] 编译通过：`mvn clean compile -DskipTests`
- [ ] 无通配符 import
- [ ] 新表字段已加 comment
- [ ] API 路径符合 RESTful 规范（复数资源名、动词用于过程操作）
- [ ] FeignClient 接口与 Controller 端点一致
