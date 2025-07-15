# DSR Frontend System ASCII Wireframes
## Comprehensive Visual Architecture of Enhanced DSR Frontend

### Overview
This document provides detailed ASCII wireframes of the complete DSR frontend system, including all enhanced components, user interfaces, and role-specific dashboards implemented through the Figma MCP integration project.

---

## 1. CITIZEN DASHBOARD LAYOUT

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ DSR HEADER                                                    [🔔] [👤] [⚙️] [🚪] │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │ 🏠 WELCOME TO DSR SERVICES                                                  │   │
│  │ Track your applications, manage benefits, access government services        │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                   │
│  │👤 Profile   │ │📋 Active    │ │💰 Active    │ │🔔 Notif     │                   │
│  │Completeness │ │Applications │ │Benefits     │ │Unread       │                   │
│  │    85%      │ │     3       │ │     2       │ │     5       │                   │
│  │[████████░░] │ │[Processing] │ │[Receiving]  │ │[New Msgs]   │                   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘                   │
│                                                                                     │
│  ┌─────────────────────────────────────────────┐ ┌─────────────────────────────┐   │
│  │ 📝 COMPLETE YOUR REGISTRATION              │ │ ⚡ QUICK ACTIONS            │   │
│  │                                             │ │                             │   │
│  │ ●────●────○────○                           │ │ ┌─────────────────────────┐ │   │
│  │ Personal  Household  Documents  Review     │ │ │👤 Update Profile        │ │   │
│  │ ✓         ✓         Current     Pending   │ │ │📄 Upload Documents      │ │   │
│  │                                             │ │ │✅ Check Eligibility     │ │   │
│  │ Complete registration to access all        │ │ │💬 Contact Support       │ │   │
│  │ services                [Continue Reg →]   │ │ └─────────────────────────┘ │   │
│  └─────────────────────────────────────────────┘ └─────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────┐ ┌─────────────────────────────┐   │
│  │ 📋 MY APPLICATIONS                          │ │ 💰 MY BENEFITS              │   │
│  │                                             │ │                             │   │
│  │ ┌─────────────────────────────────────────┐ │ │ ┌─────────────────────────┐ │   │
│  │ │ 4Ps Registration        [Review] ●      │ │ │ │ Pantawid Program        │ │   │
│  │ │ Submitted: Jan 15, 2024         [View]  │ │ │ │ Status: [Active]        │ │   │
│  │ └─────────────────────────────────────────┘ │ │ │ Monthly: ₱3,200         │ │   │
│  │                                             │ │ │ Next: Feb 15, 2024      │ │   │
│  │ ┌─────────────────────────────────────────┐ │ │ └─────────────────────────┘ │   │
│  │ │ Senior Citizen ID       [Approved] ✓    │ │ │                             │ │   │
│  │ │ Submitted: Jan 10, 2024         [View]  │ │ │ ┌─────────────────────────┐ │   │
│  │ └─────────────────────────────────────────┘ │ │ │ Senior Citizen Pension  │ │   │
│  │                                             │ │ │ Status: [Eligible]      │ │   │
│  │                           [View All Apps →] │ │ │ Monthly: ₱500           │ │   │
│  └─────────────────────────────────────────────┘ │ │ Next: Feb 1, 2024       │ │   │
│                                                   │ └─────────────────────────┘ │   │
│  ┌─────────────────────────────────────────────┐ │                             │   │
│  │ 📅 RECENT ACTIVITY                          │ │                [View All →] │   │
│  │                                             │ │                             │   │
│  │ ●─── Registration Started                   │ └─────────────────────────────┘   │
│  │ │    Jan 15, 10:30 AM - You                │                                   │
│  │ │                                          │ ┌─────────────────────────────┐   │
│  │ ●─── Documents Uploaded                    │ │ 🔔 NOTIFICATIONS            │   │
│  │ │    Jan 16, 2:15 PM - You                 │ │                             │   │
│  │ │                                          │ │ ┌─────────────────────────┐ │   │
│  │ ●─── Under Review                          │ │ │ Application Update      │ │   │
│  │      Jan 17, 9:00 AM - LGU Staff          │ │ │ Your 4Ps application... │ │   │
│  │                                             │ │ │ Jan 17, 2024            │ │   │
│  └─────────────────────────────────────────────┘ │ └─────────────────────────┘ │   │
│                                                   │                             │   │
│                                                   │ ┌─────────────────────────┐ │   │
│                                                   │ │ Document Required       │ │   │
│                                                   │ │ Please submit your...   │ │   │
│                                                   │ │ Jan 16, 2024            │ │   │
│                                                   │ └─────────────────────────┘ │   │
│                                                   │                             │   │
│                                                   │                [View All →] │   │
│                                                   └─────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. STAFF DASHBOARD LAYOUT (LGU/DSWD)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ DSR STAFF PORTAL                                          [🔔] [👤] [⚙️] [🚪]     │
├─────────────────────────────────────────────────────────────────────────────────────┤
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 🏛️ LGU STAFF DASHBOARD                                                          │ │
│ │ Manage applications, process cases, and track performance metrics              │ │
│ │                                          [Generate Report] [New Application]   │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐                                   │
│ │ 247 │ │ 24  │ │ 12  │ │  3  │ │ 4.2 │ │ 18  │                                   │
│ │Total│ │Pend │ │Appr │ │Rej  │ │Avg  │ │Mine │                                   │
│ │Apps │ │Rev  │ │Today│ │Today│ │Days │ │Assgn│                                   │
│ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘                                   │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────┐ ┌─────────────┐   │
│ │ 📋 APPLICATIONS QUEUE                    [Filter] [Export] │ │ ⚡ QUICK     │   │
│ │                                                             │ │ ACTIONS     │   │
│ │ [Search applications...]                                    │ │             │   │
│ │                                                             │ │ ┌─────────┐ │   │
│ │ ☐ Citizen Name      │Program        │Status    │Priority   │ │ │📋 Review│ │   │
│ │ ├─────────────────────────────────────────────────────────┤ │ │Apps (24)│ │   │
│ │ ☐ Juan Dela Cruz    │4Ps           │[Submit] │ Normal     │ │ └─────────┘ │   │
│ │   PSN-123456789     │              │         │           │ │             │   │
│ │                     │              │         │ 2 days    │ │ ┌─────────┐ │   │
│ │ ├─────────────────────────────────────────────────────────┤ │ │✅ Tasks │ │   │
│ │ ☐ Maria Santos      │Senior Citizen│[Review] │ High      │ │ │Mine (8) │ │   │
│ │   PSN-987654321     │Benefits      │         │           │ │ └─────────┘ │   │
│ │                     │              │         │ 3 days    │ │             │   │
│ │ ├─────────────────────────────────────────────────────────┤ │ ┌─────────┐ │   │
│ │ ☐ Pedro Gonzales    │PWD Benefits  │[Submit] │ Urgent    │ │ │🔍 Verify│ │   │
│ │   PSN-456789123     │              │         │           │ │ │Citizens │ │   │
│ │                     │              │         │ 1 day     │ │ └─────────┘ │   │
│ │ ├─────────────────────────────────────────────────────────┤ │             │   │
│ │                                                             │ │ ┌─────────┐ │   │
│ │ [Assign to Me] [Mark for Review] [Export Selected]         │ │ │📊 Local │ │   │
│ │                                                             │ │ │Reports  │ │   │
│ │ Showing 1 to 25 of 247 results    [Prev] Page 1 [Next]    │ │ └─────────┘ │   │
│ └─────────────────────────────────────────────────────────────┘ └─────────────┘   │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────┐ ┌─────────────┐   │
│ │ ✅ MY TASKS                                    [View All] │ │ 📅 RECENT   │   │
│ │                                                             │ │ ACTIVITY    │   │
│ │ ┌─────────────────────────────────────────────────────────┐ │ │             │   │
│ │ │ Review Juan's 4Ps Application        [In Progress] ●   │ │ │ ●─── App    │   │
│ │ │ Verify household composition                            │ │ │ │    Review │   │
│ │ │ Due: Jan 20, 2024                           [High]     │ │ │ │    Started│   │
│ │ └─────────────────────────────────────────────────────────┘ │ │ │           │   │
│ │                                                             │ │ ●─── Doc    │   │
│ │ ┌─────────────────────────────────────────────────────────┐ │ │ │    Upload │   │
│ │ │ Process Senior Citizen ID             [Pending] ○       │ │ │ │    Verified│   │
│ │ │ Validate submitted documents                            │ │ │ │           │   │
│ │ │ Due: Jan 18, 2024                         [Normal]     │ │ │ ●─── Status │   │
│ │ └─────────────────────────────────────────────────────────┘ │ │      Updated│   │
│ │                                                             │ │             │   │
│ │ ┌─────────────────────────────────────────────────────────┐ │ └─────────────┘   │
│ │ │ Update Beneficiary Database           [Completed] ✓     │ │                   │
│ │ │ Monthly data synchronization                            │ │                   │
│ │ │ Completed: Jan 15, 2024                   [Low]        │ │                   │
│ │ └─────────────────────────────────────────────────────────┘ │                   │
│ └─────────────────────────────────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────────────┘

---

## 3. ENHANCED COMPONENTS SHOWCASE

### A. Progress Indicator Component (Stepped Variant)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ 📝 REGISTRATION PROGRESS                                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ●────────●────────○────────○────────○                                             │
│  │        │        │        │        │                                             │
│  ✓        ✓        ●        ○        ○                                             │
│  │        │        │        │        │                                             │
│ Personal  Household Documents Eligibility Review                                   │
│ Info      Details   Upload   Check     & Submit                                    │
│ ✓ Done    ✓ Done    ● Current ○ Pending ○ Pending                                  │
│                                                                                     │
│ Basic     Family    Required  Automatic  Final                                     │
│ personal  and       ID docs   assessment review                                    │
│ details   household                                                                 │
│                                                                                     │
│                                                    [Save Draft] [Continue →]       │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### B. Progress Indicator Component (Circular Variant)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ 📊 APPLICATION PROCESSING OVERVIEW                                                  │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│                                    ████████                                        │
│                                ████        ████                                    │
│                              ██              ██                                    │
│                            ██                  ██                                  │
│                           ██        2/5         ██                                 │
│                           ██                    ██                                 │
│                            ██                  ██                                  │
│                              ██              ██                                    │
│                                ████        ████                                    │
│                                    ████████                                        │
│                                                                                     │
│                              Document Verification                                  │
│                         Currently processing documents                             │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### C. Data Table Component (Advanced Features)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ 📊 APPLICATIONS MANAGEMENT                                                          │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ [Search applications...]                                    [Filter ▼] [Export]   │
│                                                                                     │
│ ☐ Select All    [Approve Selected (3)] [Assign to Me (3)] [Export Selected (3)]   │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │☐│Citizen Name      │Program         │Status    │Priority│Days│Submitted │Actions││ │
│ ├─┼──────────────────┼────────────────┼──────────┼────────┼────┼──────────┼───────┤│ │
│ │☑│Juan Dela Cruz    │4Ps Program     │[Submit]  │Normal  │ 2  │Jan 15    │[Review││ │
│ │ │PSN-123456789     │                │          │        │days│2024      │ View] ││ │
│ ├─┼──────────────────┼────────────────┼──────────┼────────┼────┼──────────┼───────┤│ │
│ │☑│Maria Santos      │Senior Benefits │[Review]  │High    │ 3  │Jan 14    │[Review││ │
│ │ │PSN-987654321     │                │          │        │days│2024      │ View] ││ │
│ ├─┼──────────────────┼────────────────┼──────────┼────────┼────┼──────────┼───────┤│ │
│ │☑│Pedro Gonzales    │PWD Benefits    │[Submit]  │Urgent  │ 1  │Jan 16    │[Review││ │
│ │ │PSN-456789123     │                │          │        │day │2024      │ View] ││ │
│ ├─┼──────────────────┼────────────────┼──────────┼────────┼────┼──────────┼───────┤│ │
│ │☐│Ana Rodriguez     │Solo Parent     │[Approved]│Normal  │ 5  │Jan 12    │[View] ││ │
│ │ │PSN-789123456     │                │          │        │days│2024      │       ││ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ Showing 1 to 25 of 247 results                                                     │
│ [◀ Previous] Page 1 of 10 [Next ▶]                                                 │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### D. Role-Based Navigation Component (Sidebar Variant)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ 🧭 NAVIGATION                                                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ DASHBOARD                                                                           │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 🏠 Overview                                                                     │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ OPERATIONS                                                                          │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 👥 Citizens                                                                     │ │
│ │    ├── 📋 Registrations                                                         │ │
│ │    └── 🔍 Verification                                                          │ │
│ │                                                                                 │ │
│ │ 📁 Cases                                                          [New] 🔴     │ │
│ │    Manage individual and household cases                                        │ │
│ │                                                                                 │ │
│ │ 💳 Payments                                                                     │ │
│ │    Process and track benefit payments                                           │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ANALYTICS & REPORTS                                                                 │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 📊 Analytics                                                                    │ │
│ │    View system analytics and insights                                           │ │
│ │                                                                                 │ │
│ │ 📈 Reports                                                                      │ │
│ │    Generate and export reports                                                  │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

### E. Workflow Timeline Component (Detailed Variant)

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ 📅 APPLICATION TIMELINE                                                             │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ ●─── Registration Started                                    [Completed] ✓         │
│ │    Jan 15, 2024 10:30 AM                                                         │
│ │    Citizen submitted registration application                                    │
│ │    👤 Juan Dela Cruz • Citizen                                                   │
│ │    📍 Duration: 15 minutes • Location: Online Portal                            │
│ │                                                                                  │
│ ●─── Personal Information Completed                          [Completed] ✓         │
│ │    Jan 15, 2024 10:45 AM                                                         │
│ │    Basic personal details submitted and validated                                │
│ │    👤 Juan Dela Cruz • Citizen                                                   │
│ │                                                                                  │
│ ●─── Household Details Added                                 [Completed] ✓         │
│ │    Jan 15, 2024 11:00 AM                                                         │
│ │    Family composition and household information provided                         │
│ │    👤 Juan Dela Cruz • Citizen                                                   │
│ │                                                                                  │
│ ●─── Document Upload in Progress                             [Current] ●           │
│ │    Jan 15, 2024 11:15 AM                                                         │
│ │    Uploading required identification documents                                   │
│ │    👤 Juan Dela Cruz • Citizen                                                   │
│ │    📎 Attachments: birth_certificate.pdf, valid_id.jpg                          │
│ │    [Continue Upload] [Save Draft]                                               │
│ │                                                                                  │
│ ○─── Document Verification                                   [Pending] ○           │
│      Pending - LGU staff will review submitted documents                          │
│      📍 Estimated: 2-3 business days                                              │
│                                                                                     │
│ ○─── Eligibility Assessment                                  [Pending] ○           │
│      Pending - Automatic eligibility check                                        │
│                                                                                     │
│ ○─── Final Review & Approval                                 [Pending] ○           │
│      Pending - Final review by authorized personnel                                │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. REGISTRATION WORKFLOW PAGES

### A. Multi-Step Registration Form

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ DSR REGISTRATION - STEP 3 OF 5                                   [Save] [Exit]     │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ ●────●────●────○────○                                                               │
│ Personal  Household  Documents  Eligibility  Review                                │
│ ✓ Done    ✓ Done     ● Current  ○ Pending    ○ Pending                             │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 📄 DOCUMENT UPLOAD                                                              │ │
│ │                                                                                 │ │
│ │ Required Documents:                                                             │ │
│ │                                                                                 │ │
│ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 📋 Birth Certificate                                          [Required]    │ │ │
│ │ │ ┌─────────────────────────────────────────────────────────────────────────┐ │ │ │
│ │ │ │ 📎 birth_certificate.pdf                                    [✓ Uploaded]│ │ │ │
│ │ │ │ Size: 2.3 MB • Uploaded: Jan 15, 2024 11:20 AM             [Remove]    │ │ │ │
│ │ │ └─────────────────────────────────────────────────────────────────────────┘ │ │ │
│ │ └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                 │ │
│ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 🆔 Valid Government ID                                        [Required]    │ │ │
│ │ │ ┌─────────────────────────────────────────────────────────────────────────┐ │ │ │
│ │ │ │ [📁 Drag & Drop files here or click to browse]                         │ │ │ │
│ │ │ │ Accepted formats: PDF, JPG, PNG • Max size: 5MB                        │ │ │ │
│ │ │ └─────────────────────────────────────────────────────────────────────────┘ │ │ │
│ │ └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                 │ │
│ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ 🏠 Proof of Residence                                        [Optional]     │ │ │
│ │ │ ┌─────────────────────────────────────────────────────────────────────────┐ │ │ │
│ │ │ │ [📁 Drag & Drop files here or click to browse]                         │ │ │ │
│ │ │ │ Utility bill, barangay certificate, etc.                               │ │ │ │
│ │ │ └─────────────────────────────────────────────────────────────────────────┘ │ │ │
│ │ └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                 │ │
│ │ Upload Progress: 1 of 3 documents uploaded                                     │ │
│ │ [████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░] 33%   │ │
│ │                                                                                 │ │
│ │ ⚠️ Note: All required documents must be uploaded to proceed to the next step   │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│                                                    [◀ Previous] [Save Draft] [Next ▶] │
└─────────────────────────────────────────────────────────────────────────────────────┘

### B. Application Review & Status Page

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ APPLICATION DETAILS - 4Ps Registration #APP-2024-001234                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 📋 APPLICATION SUMMARY                                          [Under Review] │ │
│ │                                                                                 │ │
│ │ Applicant: Juan Dela Cruz                    Program: Pantawid Pamilyang       │ │
│ │ PSN: 123456789                                        Pilipino Program (4Ps)   │ │
│ │ Submitted: January 15, 2024                  Priority: Normal                  │ │
│ │ Last Updated: January 17, 2024               Assigned: Maria Santos (LGU)      │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 📊 STATUS BADGES                                                                │ │
│ │                                                                                 │ │
│ │ [Submitted] ✓  [Documents] ✓  [Review] ●  [Eligibility] ○  [Approval] ○        │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 👤 APPLICANT INFORMATION                                                        │ │
│ │                                                                                 │ │
│ │ Full Name: Juan Dela Cruz                    Civil Status: Married             │ │
│ │ Date of Birth: March 15, 1985               Gender: Male                       │ │
│ │ Contact: +63 912 345 6789                   Email: juan.delacruz@email.com     │ │
│ │ Address: 123 Rizal Street, Barangay San Jose, Quezon City, Metro Manila       │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 🏠 HOUSEHOLD COMPOSITION                                                        │ │
│ │                                                                                 │ │
│ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │ │Name              │Relationship│Age│Gender│Education    │Employment         ││ │ │
│ │ ├─────────────────────────────────────────────────────────────────────────────┤│ │ │
│ │ │Juan Dela Cruz    │Head        │38 │Male  │High School  │Construction Worker││ │ │
│ │ │Maria Dela Cruz   │Spouse      │35 │Female│Elementary   │Housewife          ││ │ │
│ │ │Ana Dela Cruz     │Child       │12 │Female│Grade 6      │Student            ││ │ │
│ │ │Pedro Dela Cruz   │Child       │8  │Male  │Grade 2      │Student            ││ │ │
│ │ └─────────────────────────────────────────────────────────────────────────────┘│ │ │
│ │                                                                                 │ │
│ │ Total Household Members: 4                   Monthly Income: ₱8,500             │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 📄 SUBMITTED DOCUMENTS                                                          │ │
│ │                                                                                 │ │
│ │ ✓ Birth Certificate (juan_birth_cert.pdf)                      [View] [Download]│ │
│ │ ✓ Valid Government ID (juan_valid_id.jpg)                      [View] [Download]│ │
│ │ ✓ Marriage Certificate (marriage_cert.pdf)                     [View] [Download]│ │
│ │ ✓ Children's Birth Certificates (children_birth_certs.pdf)     [View] [Download]│ │
│ │ ○ Proof of Income (Optional)                                   [Request Upload] │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ 📝 STAFF NOTES & ACTIONS                                                        │ │
│ │                                                                                 │ │
│ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ Jan 17, 2024 - Maria Santos (LGU Staff)                                    │ │ │
│ │ │ Documents verified. All required documents are present and valid.           │ │ │
│ │ │ Proceeding to eligibility assessment.                                       │ │ │
│ │ └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                 │ │
│ │ ┌─────────────────────────────────────────────────────────────────────────────┐ │ │
│ │ │ [Add Note...]                                                               │ │ │
│ │ └─────────────────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                                 │ │
│ │ [Approve Application] [Request More Info] [Reject Application] [Assign to...]  │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│                                                              [◀ Back to Queue]     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 5. MOBILE RESPONSIVE LAYOUTS

### A. Mobile Citizen Dashboard (320px width)

```
┌─────────────────────────────────┐
│ DSR Services        [🔔] [👤] │
├─────────────────────────────────┤
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 🏠 Welcome to DSR Services  │ │
│ │ Track applications & manage │ │
│ │ your benefits               │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────┐ ┌─────────────┐ │
│ │👤 Profile   │ │📋 Active    │ │
│ │Completeness │ │Applications │ │
│ │    85%      │ │     3       │ │
│ │[████████░░] │ │[Processing] │ │
│ └─────────────┘ └─────────────┘ │
│                                 │
│ ┌─────────────┐ ┌─────────────┐ │
│ │💰 Active    │ │🔔 Notif     │ │
│ │Benefits     │ │Unread       │ │
│ │     2       │ │     5       │ │
│ │[Receiving]  │ │[New Msgs]   │ │
│ └─────────────┘ └─────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 📝 REGISTRATION PROGRESS    │ │
│ │                             │ │
│ │ ●──●──○──○                  │ │
│ │ Personal  Documents         │ │
│ │ ✓ Done    ● Current         │ │
│ │                             │ │
│ │ Complete your registration  │ │
│ │ to access all services      │ │
│ │                             │ │
│ │        [Continue →]         │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ ⚡ QUICK ACTIONS             │ │
│ │                             │ │
│ │ ┌─────────────────────────┐ │ │
│ │ │👤 Update Profile        │ │ │
│ │ └─────────────────────────┘ │ │
│ │ ┌─────────────────────────┐ │ │
│ │ │📄 Upload Documents      │ │ │
│ │ └─────────────────────────┘ │ │
│ │ ┌─────────────────────────┐ │ │
│ │ │✅ Check Eligibility     │ │ │
│ │ └─────────────────────────┘ │ │
│ │ ┌─────────────────────────┐ │ │
│ │ │💬 Contact Support       │ │ │
│ │ └─────────────────────────┘ │ │
│ └─────────────────────────────┘ │
│                                 │
│ ┌─────────────────────────────┐ │
│ │ 📋 MY APPLICATIONS          │ │
│ │                             │ │
│ │ ┌─────────────────────────┐ │ │
│ │ │ 4Ps Registration        │ │ │
│ │ │ [Review] ●              │ │ │
│ │ │ Jan 15, 2024    [View]  │ │ │
│ │ └─────────────────────────┘ │ │
│ │                             │ │
│ │ ┌─────────────────────────┐ │ │
│ │ │ Senior Citizen ID       │ │ │
│ │ │ [Approved] ✓            │ │ │
│ │ │ Jan 10, 2024    [View]  │ │ │
│ │ └─────────────────────────┘ │ │
│ │                             │ │
│ │           [View All →]      │ │
│ └─────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘

### B. Mobile Staff Interface (768px tablet width)

```
┌─────────────────────────────────────────────────────────────────────────┐
│ DSR Staff Portal                                    [🔔] [👤] [⚙️] [🚪] │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│ ┌─────────────────────────────────────────────────────────────────────┐ │
│ │ 🏛️ LGU STAFF DASHBOARD                                              │ │
│ │ Manage applications and track performance                           │ │
│ │                                          [Reports] [New App]        │ │
│ └─────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐                       │
│ │ 247 │ │ 24  │ │ 12  │ │  3  │ │ 4.2 │ │ 18  │                       │
│ │Total│ │Pend │ │Appr │ │Rej  │ │Avg  │ │Mine │                       │
│ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘ └─────┘                       │
│                                                                         │
│ ┌─────────────────────────────────────────────────────────────────────┐ │
│ │ 📋 APPLICATIONS QUEUE                            [Filter] [Export] │ │
│ │                                                                     │ │
│ │ [Search applications...]                                            │ │
│ │                                                                     │ │
│ │ ┌─────────────────────────────────────────────────────────────────┐ │ │
│ │ │☐│Citizen Name    │Program      │Status  │Priority│Days│Actions ││ │ │
│ │ ├─┼────────────────┼─────────────┼────────┼────────┼────┼────────┤│ │ │
│ │ │☐│Juan Dela Cruz  │4Ps Program  │[Submit]│Normal  │ 2  │[Review]││ │ │
│ │ │ │PSN-123456789   │             │        │        │days│ [View] ││ │ │
│ │ ├─┼────────────────┼─────────────┼────────┼────────┼────┼────────┤│ │ │
│ │ │☐│Maria Santos    │Senior Benef │[Review]│High    │ 3  │[Review]││ │ │
│ │ │ │PSN-987654321   │             │        │        │days│ [View] ││ │ │
│ │ ├─┼────────────────┼─────────────┼────────┼────────┼────┼────────┤│ │ │
│ │ │☐│Pedro Gonzales  │PWD Benefits │[Submit]│Urgent  │ 1  │[Review]││ │ │
│ │ │ │PSN-456789123   │             │        │        │day │ [View] ││ │ │
│ │ └─────────────────────────────────────────────────────────────────┘ │ │
│ │                                                                     │ │
│ │ [Assign to Me] [Mark for Review] [Export Selected]                 │ │
│ │                                                                     │ │
│ │ Showing 1 to 10 of 247 results        [Prev] Page 1 [Next]        │ │
│ └─────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│ ┌─────────────────────────────────────────────────────────────────────┐ │
│ │ ⚡ QUICK ACTIONS                    │ 📅 RECENT ACTIVITY             │ │
│ │                                     │                                │ │
│ │ ┌─────────────────────────────────┐ │ ●─── Application Review        │ │
│ │ │📋 Review Applications (24)      │ │ │    Started                    │ │
│ │ └─────────────────────────────────┘ │ │                               │ │
│ │                                     │ ●─── Document Upload           │ │
│ │ ┌─────────────────────────────────┐ │ │    Verified                   │ │
│ │ │✅ My Tasks (8)                  │ │ │                               │ │
│ │ └─────────────────────────────────┘ │ ●─── Status Updated            │ │
│ │                                     │                                │ │
│ │ ┌─────────────────────────────────┐ │                                │ │
│ │ │🔍 Verify Citizens               │ │                                │ │
│ │ └─────────────────────────────────┘ │                                │ │
│ │                                     │                                │ │
│ │ ┌─────────────────────────────────┐ │                                │ │
│ │ │📊 Local Reports                 │ │                                │ │
│ │ └─────────────────────────────────┘ │                                │ │
│ └─────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 6. COMPONENT INTERACTION FLOWS

### A. Status Badge Integration Across Components

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ STATUS BADGE ECOSYSTEM                                                              │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ Progress Indicator + Status Badges:                                                 │
│ ●────●────●────○────○                                                               │
│ [Completed] [Completed] [Processing] [Pending] [Pending]                           │
│                                                                                     │
│ Data Table + Status Badges:                                                        │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ Juan Dela Cruz    │ 4Ps Program     │ [Submitted] │ [Normal]  │ 2 days          │ │
│ │ Maria Santos      │ Senior Benefits │ [Review]    │ [High]    │ 3 days          │ │
│ │ Pedro Gonzales    │ PWD Benefits    │ [Submitted] │ [Urgent]  │ 1 day           │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ Timeline + Status Badges:                                                          │
│ ●─── Registration Started                                    [Completed] ✓         │
│ ●─── Documents Uploaded                                      [Completed] ✓         │
│ ●─── Under Review                                            [Processing] ●        │
│ ○─── Eligibility Check                                       [Pending] ○           │
│                                                                                     │
│ Dashboard Cards + Status Badges:                                                   │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                   │
│ │ Profile     │ │ Applications│ │ Benefits    │ │ Notifications│                   │
│ │ [85% Done]  │ │ [Processing]│ │ [Active]    │ │ [New Msgs]  │                   │
│ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘                   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### B. Navigation Flow Between Components

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ COMPONENT NAVIGATION FLOW                                                           │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ Role-Based Navigation → Dashboard → Detailed Views                                  │
│                                                                                     │
│ ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐           │
│ │ 🧭 Nav Menu │ →  │ 🏠 Dashboard│ →  │ 📋 App List │ →  │ 📄 App Detail│           │
│ │             │    │             │    │             │    │             │           │
│ │ • Citizens  │    │ • Overview  │    │ • DataTable │    │ • Timeline  │           │
│ │ • Cases     │    │ • Metrics   │    │ • Filters   │    │ • Progress  │           │
│ │ • Reports   │    │ • Quick     │    │ • Search    │    │ • Actions   │           │
│ │             │    │   Actions   │    │ • Bulk Ops  │    │             │           │
│ └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘           │
│                                                                                     │
│ Citizen Flow:                                                                       │
│ Registration → Progress Tracking → Status Updates → Timeline View                  │
│                                                                                     │
│ ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐           │
│ │ 📝 Register │ →  │ 📊 Progress │ →  │ 🔔 Updates  │ →  │ 📅 Timeline │           │
│ │             │    │             │    │             │    │             │           │
│ │ • Multi-step│    │ • Step      │    │ • Status    │    │ • Event     │           │
│ │ • Validation│    │   Indicator │    │   Badges    │    │   History   │           │
│ │ • Auto-save │    │ • Completion│    │ • Real-time │    │ • Actor     │           │
│ │             │    │   %         │    │   Updates   │    │   Info      │           │
│ └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘           │
│                                                                                     │
│ Staff Flow:                                                                         │
│ Queue Management → Application Review → Decision Making → Status Update            │
│                                                                                     │
│ ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐           │
│ │ 📊 DataTable│ →  │ 📄 App View │ →  │ ⚖️ Decision │ →  │ 📝 Update   │           │
│ │             │    │             │    │             │    │             │           │
│ │ • Filtering │    │ • Full      │    │ • Approve   │    │ • Status    │           │
│ │ • Sorting   │    │   Details   │    │ • Reject    │    │   Change    │           │
│ │ • Bulk Ops  │    │ • Timeline  │    │ • Request   │    │ • Timeline  │           │
│ │ • Search    │    │ • Documents │    │   Info      │    │   Entry     │           │
│ └─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘           │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 7. ACCESSIBILITY FEATURES VISUALIZATION

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│ ACCESSIBILITY FEATURES ACROSS ALL COMPONENTS                                       │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│ Keyboard Navigation Flow:                                                           │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ [Tab] → Navigation Menu → [Tab] → Dashboard Cards → [Tab] → Action Buttons      │ │
│ │         ↓                         ↓                         ↓                   │ │
│ │ [Enter] to select      [Enter] to view details   [Enter] to execute             │ │
│ │ [Arrow Keys] to move   [Space] to select         [Esc] to cancel                │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ Focus Indicators:                                                                   │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                  │ │
│ │ │ [Button]        │  │ ╔═══════════════╗ │  │ [Button]        │                  │ │
│ │ │ Normal State    │  │ ║ [Button]      ║ │  │ Normal State    │                  │ │
│ │ └─────────────────┘  │ ║ Focused State ║ │  └─────────────────┘                  │ │
│ │                      │ ╚═══════════════╝ │                                       │ │
│ │                      │ 2px blue outline  │                                       │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ Screen Reader Support:                                                              │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ Status Badge: "Status: Submitted, Application submitted for review"             │ │
│ │ Progress Step: "Step 2 of 5: Document Upload, Current step"                     │ │
│ │ Data Table: "Table with 247 applications, sortable by name, status, date"      │ │
│ │ Timeline Event: "Event 1 of 4: Registration started, completed January 15"     │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ High Contrast Mode:                                                                 │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ Normal Mode:           High Contrast Mode:                                      │ │
│ │ ┌─────────────┐       ┌─────────────┐                                          │ │
│ │ │ [Submitted] │  →    │ [SUBMITTED] │                                          │ │
│ │ │ Blue bg     │       │ Black bg    │                                          │ │
│ │ │ White text  │       │ White text  │                                          │ │
│ │ └─────────────┘       └─────────────┘                                          │ │
│ │ 4.5:1 contrast        7:1 contrast                                             │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
│ Touch Target Sizes (Mobile):                                                       │
│ ┌─────────────────────────────────────────────────────────────────────────────────┐ │
│ │ ┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐      │ │
│ │ │                     │  │                     │  │                     │      │ │
│ │ │      [Button]       │  │      [Button]       │  │      [Button]       │      │ │
│ │ │     44px × 44px     │  │     44px × 44px     │  │     44px × 44px     │      │ │
│ │ │                     │  │                     │  │                     │      │ │
│ │ └─────────────────────┘  └─────────────────────┘  └─────────────────────┘      │ │
│ │ Minimum touch target size for accessibility compliance                          │ │
│ └─────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Summary

This comprehensive ASCII wireframe documentation covers:

1. **Complete User Interfaces**: Citizen and Staff dashboards with all enhanced components
2. **Enhanced Components**: Detailed wireframes of ProgressIndicator, DataTable, RoleBasedNavigation, and WorkflowTimeline
3. **Registration Workflows**: Multi-step forms with document upload and status tracking
4. **Mobile Responsive**: Layouts optimized for 320px mobile and 768px tablet breakpoints
5. **Component Integration**: How all components work together in real workflows
6. **Accessibility Features**: Visual representation of keyboard navigation, focus indicators, and screen reader support

The wireframes demonstrate the complete DSR frontend system with government-appropriate design, comprehensive accessibility features, and role-based optimization for citizens, LGU staff, and DSWD staff.
```
```
```
```