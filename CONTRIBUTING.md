# Contribute to Femas

**`English` | [`简体中文`](./CONTRIBUTING-zh.md)**

Welcome to join us! This document is a guide on how to contribute to Femas.

If you have good comments or suggestions, please create [`Issues`](https://github.com/Tencent/Femas/issues/new) or [`Pull Requests`](https://github.com/Tencent/Femas/pulls), to contribute to the Femas open source community. Femas continues to recruit contributors, even if it is answering questions in the issue, or doing some simple bug fixes, it will be of great help to us.

[Tencent Open Source Incentive Program](https://opensource.tencent.com/contribution) encourages developers to participate in contributions, and look forward to your joining.

-------------------


## code of conduct
> Please be sure to read and follow our **[Code of Conduct](./Code-of-Conduct.md)**.

## Report Bugs/Submit an issue
* Search for [issues](https://github.com/Tencent/Femas/issues) to ensure that the error has not been raised, so as to avoid repeated submissions.
* Get in touch with the community through the community WeChat group or email to confirm bugs or new features (optional).
* [Create a new issue](https://github.com/Tencent/Femas/issues/new) , including **title and detailed description, code examples, screenshots or short videos** to help us locate the issue .
## Start contributing
Femas community contributors mainly have four types of roles:
- Users (community users who research in the corporate environment or who are currently landing Femas)
- Contributors (people who bring value to the project or merge PR)
- Submitters (PR reaches a certain number and will be promoted to the submitter role by the community jury and become a backbone member of the community)
- PMC (Project Manager)

We encourage newcomers to actively participate in the Femas project. We also welcome corporate users to use Femas and provide the community with landing cases. The community will fully support the corporate users in landing and share wonderful cases with everyone. The spirit of the Internet is sharing, and the Femas community We will continue to share Tencent's internal microservice best practice methodology with the industry.

### Pull Request Guide
> First select an issue that you want to fix from [issues list](https://github.com/Tencent/Femas/issues), any PR must be associated with a valid issue, or first with community members Contact and confirm that the issue is unclaimed. If multiple people claim the same issue at the same time, the community will review the issue and assign the issue to the most reasonable maintainer.

Femas uses the **`develop`** branch as the development benchmark branch. The following is the specific operation process:
1. Fork the code from **`develop` branch** to your own GitHub repository.
2. Create a new branch, the branch name can be named with issue number, for example (fix_issue#007).
3. Submit the changes, please confirm your branch and source branch code synchronization before submitting.
4. Push your commit to your fork repository.
5. Create a pull request to the **`develop` branch**.

Matters needing attention when submitting PR:
- Please refer to [Pull Request Template](./PULL_REQUEST_TEMPLATE.md).
- Please confirm that the PR is associated with a valid issue, otherwise the PR will be rejected.
- Please add a license and copyright notice to the newly created file.
- Ensure a consistent code style and do adequate testing.
- If the PR contains a lot of changes, such as component refactoring or new components, please elaborate on its design and usage.
- Ensure that the submitted information is as clear and concise as possible.

### Code review
All code merged into the official code will be reviewed by the community. Generally, these tasks will be assigned to Committer or PMC. Of course, we will also avoid some messy code styles or potential vulnerabilities through automated methods. We must keep the entire code warehouse high. Quality Standard:
> - Readability: Important code should have detailed comments and documentation, and API should have Javadoc.
> - Elegance: Common components and functions must have a reasonable design and have good scalability. Standardized and modular code design is the core of Femas.
> - Maintainability: to ensure a consistent code style, in line with our [Code Style](style/codeStyle.md).
> - Testability: The relevant code of the core feature should be covered by unit test cases.
    
## Questions about the source code
#### [FAQ]()
#### Join our community group
![image](https://user-images.githubusercontent.com/22976760/153164965-ff5d0f2b-5990-4c8e-a7dc-2791fd1ca8bd.png)

