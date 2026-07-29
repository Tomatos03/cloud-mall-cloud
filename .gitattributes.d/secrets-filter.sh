#!/bin/sh
# git clean filter: 提交前自动将密钥替换为占位符
# 配合 .gitattributes 的 filter=strip-secrets 使用
#
# 添加git配置：
#   git config filter.strip-secrets.clean \
#     '.gitattributes.d/secrets-filter.sh'
#   git config filter.strip-secrets.smudge cat
#
# 原理：git add 时管道经过此脚本，匹配到的密钥模式被替换，
#       检出（smudge）时原样通过，不影响工作区文件。

sed \
  -e 's/sk-or-v1-[a-f0-9]\{32,\}/<openrouter-api-key>/g' \
  -e 's/sk-[a-zA-Z0-9]\{20,\}/<openai-api-key>/g' \
  -e 's/ghp_[a-zA-Z0-9]\{36,\}/<github-pat>/g' \
  -e 's/gho_[a-zA-Z0-9]\{36,\}/<github-oauth>/g' \
  -e 's/ghu_[a-zA-Z0-9]\{36,\}/<github-user-token>/g' \
  -e 's/AKIA[0-9A-Z]\{16\}/<aws-akid>/g' \
  "$@"
